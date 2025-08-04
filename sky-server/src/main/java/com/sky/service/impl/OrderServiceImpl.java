package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;


    @Autowired // websocket连接
    private WebSocketServer webSocketServer;

    // 跳过微信后，方法中没有订单号，就无法根据订单号获取订单id进而更新  所以在创建订单时就存在类里
    // 在submit方法中给他赋值
    private Long orderId;
    private String orderNumber;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 1.下单前处理各种业务异常(地址簿为空  购物车为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = ShoppingCart.builder().userId(userId).build();
        ArrayList<ShoppingCart> shoppingCarts = shoppingCartMapper.list(cart);
        if(shoppingCarts==null||shoppingCarts.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        // 2.向订单表插入一条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);

        // 订单号就用当前的时间戳
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
//        order.setUserName(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
//        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());

        // 用户名称 地址  结账时间 订单取消原因，订单拒绝原因 订单取消时间
        //  送达时间 都没设置
        orderMapper.insert(order);

        // 为了后续能直接拿到id就直接存在类中
        this.orderId=order.getId();
        this.orderNumber=order.getNumber();


        ArrayList<OrderDetail> orderDetailArrayList = new ArrayList<>();
        // 3.向订单明细表插入n条数据
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailArrayList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailArrayList);

        // 4.下单成功后清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        // 5.封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime()).build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);


        // 如果通过微信后端先预支付，这里微信后端会返回一些参数(timeStamp,nonceStr,package，signType,paySign)
        // 将其封装后给小程序用于正式下单
/*        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

 */

        // 跳过微信支付和后面微信客户端调用的支付成功本地后端接口(notify)，直接修改状态
        // 直接跳到PayNotifyController中的paySuccessNotify方法中最后的代码orderService.paySuccess(outTradeNo)方法
        // 参数为下一个方法里的id,status,payStatus,checkoutTime
        // 此时还有一个问题，就是跳过后，这里没有订单号，就无法根据订单号获取订单id进而更新  所以在创建订单时就存在类里

        JSONObject jsonObject = new JSONObject();
        // 直接设置code为已支付
        jsonObject.put("code","ORDERPAID");

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        // 这里跳过后对应的package字段就是空了(对应的是预支付订单id)
//        vo.setPackageStr(jsonObject.getString("package"));


        Integer orderStatus = Orders.TO_BE_CONFIRMED;// 订单状态，待接单
        Integer orderPaidStatus = Orders.PAID;// 支付状态，已支付
        LocalDateTime check_out_time = LocalDateTime.now();// 更新结账时间
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(this.orderId)
                .status(orderStatus)
                .payStatus(orderPaidStatus)
                .checkoutTime(check_out_time)
                .build();
        orderMapper.update(orders);

        // websocket新增
        // 通过websocket向客户端浏览器推送消息type orderId content
        HashMap map = new HashMap();
        map.put("type",1);// 1表示来单提醒 2表示客户催单
        map.put("orderId",this.orderId);
        map.put("content","订单号，"+this.orderNumber);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);


        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // websocket新增
        // 通过websocket向客户端浏览器推送消息type orderId content
        HashMap map = new HashMap();
        map.put("type",1);// 1表示来单提醒 2表示客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号，"+outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    /**
     * 用户催单
     * @param id
     */
    public void reminder(Long id) {
        // 根据订单id查询订单
        Orders order = orderMapper.getById(id);

        // 不存在
        if(order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // websocket
        // 通过websocket向客户端浏览器推送消息type orderId content
        HashMap map = new HashMap();
        map.put("type",2);// 1表示来单提醒 2表示客户催单
        map.put("orderId",id);
        map.put("content","订单号，"+order.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult historyOrders(Integer page, Integer pageSize, Integer status) {

        // 有可选参数status，所以用个实体
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        PageHelper.startPage(page,pageSize);
        // 先拿到order
        Page<Orders> orderPage=orderMapper.historyOrders(ordersPageQueryDTO);
        // 再拿到每个order对应的详细信息   最后封装为OrderVO返回
        List<OrderVO> orderVOList = new ArrayList<>();
        if(orderPage!=null&&orderPage.getTotal()>0){
            for (Orders order : orderPage) {
                Long orderId = order.getId();
                List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(orderId);
                
                // 创建临时的VO以存储
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(orderDetails);
                orderVOList.add(orderVO);
            }
        }

        return new PageResult(orderPage.getTotal(),orderVOList);
    }

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    public OrderVO orderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        // 1.查询order表得到基础信息
        Orders order=orderMapper.getById(id);

        BeanUtils.copyProperties(order,orderVO);
        // 2.查询order_details表得到详细信息
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orderVO.getId());
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    public void cancel(Long id) {
        // 先查询订单，得到订单状态
        Orders orders=orderMapper.getById(id);
        if(orders==null){
            // 订单不存在
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 只有接单之前才能直接取消,其他需要与商家协商，让商家改变订单状态后才能取消
        if(orders.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // Todo 没有做微信的功能，但如果是待接单状态的取消，需要进行退款
//        // 订单处于待接单状态下取消，需要进行退款
//        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
//            //支付状态修改为 退款
//            orders.setPayStatus(Orders.REFUND);
//        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    public void repetition(Long id) {
        // 再来一单就是查询出订单的详细信息，并把信息放到购物车中
        // 查询出订单的详细信息
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        // 把订单详情对象转化为购物车对象(购物车实体与order_detail实体仅仅多了一个create_time和user_id)
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart cart = new ShoppingCart();
            // 忽略id，因为新增时id是自增的
            BeanUtils.copyProperties(orderDetail,cart,"id");
            cart.setCreateTime(LocalDateTime.now());
            cart.setUserId(BaseContext.getCurrentId());
            shoppingCarts.add(cart);
        }

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCarts);
    }
}
