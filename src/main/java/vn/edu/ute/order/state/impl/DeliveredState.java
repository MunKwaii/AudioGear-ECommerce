package vn.edu.ute.order.state.impl;

import vn.edu.ute.order.state.OrderState;

/**
 * Trạng thái Cuối cùng (Terminal State) số 1: Hoàn Tất.
 * Một khi Đơn hàng đạt trạng thái này rồi thì KHÔNG THỂ kích hoạt bất kỳ sự thay đổi (transition) nào nữa.
 * Mọi hàm thao tác sẽ tự động kích hoạt tính năng văng lỗi Default trong OrderState.
 */
public class DeliveredState implements OrderState {
    // Để trống. Không override gì cả để được thừa kế lỗi IllegalStateException mặc định của Java 8 Default Interface.
}
