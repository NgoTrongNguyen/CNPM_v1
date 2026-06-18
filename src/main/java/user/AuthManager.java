package user;

import java.util.List;

public class AuthManager {
    private static SessionUser currentUser = null;
    public static void login(SessionUser user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static SessionUser getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasManagerRole(){
        if(currentUser.getRole().equals("ADMIN")){return true;}
        return currentUser.getRole().contains("MANAGER");
    }

    public static boolean hasAdminRole(){
        return currentUser.getRole().equals("ADMIN");
    }

    public static boolean hasResidentRole(){
        if(currentUser.getRole().equals("ADMIN")){return true;}
        return currentUser.getRole().contains("RESIDENT");
    }

    public static boolean hasFinanceRole(){
        if(currentUser.getRole().equals("ADMIN")){return true;}
        return currentUser.getRole().contains("FINANCE");
    }

    /**
     * Kiểm tra user hiện tại có quyền truy cập trang quản lý nhân sự không.
     * Đầu vào: không có (lấy từ currentUser)
     * Đầu ra: true nếu role là ADMIN, RESIDENT, hoặc FINANCE
     */
    public static boolean hasStaffManagerRole() {
        if (currentUser == null) return false;
        String role = currentUser.getRole();
        return role.equals("ADMIN") || role.equals("RESIDENT") || role.equals("FINANCE");
    }

    /**
     * Trả về danh sách các role mà user hiện tại được phép quản lý.
     * - ADMIN: quản lý tất cả roles (ADMIN, MANAGER, RESIDENT, FINANCE)
     * - RESIDENT: chỉ quản lý staff role RESIDENT
     * - FINANCE: chỉ quản lý staff role FINANCE
     * Đầu vào: không có (lấy từ currentUser)
     * Đầu ra: List<String> các role được phép quản lý
     */
    public static List<String> getManageableRoles() {
        if (currentUser == null) return List.of();
        String role = currentUser.getRole();
        return switch (role) {
            case "ADMIN" -> List.of("ADMIN", "MANAGER", "RESIDENT", "FINANCE");
            case "RESIDENT" -> List.of("RESIDENT");
            case "FINANCE" -> List.of("FINANCE");
            default -> List.of();
        };
    }

}
