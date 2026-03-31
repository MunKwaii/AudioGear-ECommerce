package vn.edu.ute.order.state.impl;

import vn.edu.ute.order.state.OrderState;

/**
 * Trạng thái Cuối cùng (Terminal State) số 2: Đã Huỷ.
 * Đơn đã huỷ thì không thể quay trở về Pending hay Shipping được nữa.
 */
public class CancelledState implements OrderState {
   // Để trống. Hưởng thừa kế ném Exception từ interface.
}
