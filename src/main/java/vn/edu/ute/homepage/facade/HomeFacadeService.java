package vn.edu.ute.homepage.facade;

import vn.edu.ute.dto.HomePageDTO;

public interface HomeFacadeService {
    /**
     * Lấy toàn bộ dữ liệu cần thiết cho trang chủ đóng gói vào HomePageDTO
     * @return HomePageDTO
     */
    HomePageDTO getHomePageData();
}
