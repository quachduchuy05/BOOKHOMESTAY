package BookingHomeStay.BookingHomeStay;

import BookingHomeStay.BookingHomeStay.repository.HomestayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import BookingHomeStay.BookingHomeStay.entity.Homestay;

@SpringBootTest
public class PrintDbTest {

    @Autowired
    private HomestayRepository repo;

    @Test
    public void printHomestays() {
        System.out.println("====== HOMESTAYS ======");
        for(Homestay h : repo.findAll()) {
            System.out.println(h.getName() + " - " + h.getProvince() + " - " + h.getDistrict());
        }
        System.out.println("=======================");
    }
}
