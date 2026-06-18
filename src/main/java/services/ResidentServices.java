package services;

import models.Resident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ResidentRepo;
import user.AuthManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResidentServices {
    private final ResidentRepo residentRepo;
    private static final Logger logger  = LoggerFactory.getLogger(ResidentServices.class);

    public ResidentServices(ResidentRepo residentRepo) {
        this.residentRepo = residentRepo;
    }

    public List<Resident> findAll() {
        if(AuthManager.isLoggedIn()) {
            logger.info("user has role for finding all Residents");
            return residentRepo.findAll();
        }
        logger.info("user doesn't have role for finding all Residents");
        return null;
    }

    private LocalDate convertStringToLocalDate(String date) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        // Tìm chuỗi khớp pattern ngày tháng trong input
        Pattern datePattern = Pattern.compile(
                "\\d{1,4}[/\\-]\\d{1,2}[/\\-]\\d{2,4}"
        );
        Matcher matcher = datePattern.matcher(date.trim());
        if (!matcher.find()) return null;

        String extracted = matcher.group();
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(extracted, formatter);
            } catch (DateTimeParseException ignored) {}
        }

        logger.warn("Không thể parse ngày: {}", extracted);
        return null;
    }

    public List<Resident> findByContainInfo(String info) {
        if (info == null || info.isBlank()) {
            return residentRepo.findAll();
        }

        String trimmed = info.trim();

        // Thử parse ngày tháng trước
        LocalDate localDate = convertStringToLocalDate(trimmed);
        if (localDate != null) {
            return residentRepo.findByDate(localDate);
        }

        // Chuỗi chỉ gồm chữ số → tìm theo CCCD hoặc SĐT
        if (trimmed.matches("^[0-9]+$")) {
            return residentRepo.findWithNumber(trimmed);
        }

        // Còn lại (chỉ chữ, hỗn hợp, có dấu cách...) → tìm theo tên
        return residentRepo.findWithName(trimmed);
    }

    public boolean addResident(Resident resident){
        if(residentRepo.findById(resident.getResidentId()) == null){
            try{
                residentRepo.addResident(resident);
                return true;
            }catch (Exception e){
                logger.error(e.getMessage());
                return false;
            }
        }return false;
    }

    public boolean deleteResident(Resident resident){
        if(residentRepo.findById(resident.getResidentId()) != null){
            try{
                residentRepo.deleteResident(resident);
                return true;
            }catch (Exception e){
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
