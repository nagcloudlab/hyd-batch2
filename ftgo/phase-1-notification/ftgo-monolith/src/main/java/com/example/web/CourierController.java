package com.example.web;

import com.example.entity.Delivery;
import com.example.repository.CourierRepository;
import com.example.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/courier")
@RequiredArgsConstructor
public class CourierController {

    private final DeliveryService deliveryService;
    private final CourierRepository courierRepository;

    // Page 1: Available deliveries to accept
    @GetMapping("/available")
    public String availableDeliveries(@RequestParam(defaultValue = "1") Long courierId, Model model) {
        List<Delivery> pending = deliveryService.getPendingDeliveries();
        model.addAttribute("deliveries", pending);
        model.addAttribute("courierId", courierId);
        model.addAttribute("couriers", courierRepository.findAll());
        return "courier/available";
    }

    @PostMapping("/accept")
    public String acceptDelivery(@RequestParam Long deliveryId, @RequestParam Long courierId) {
        deliveryService.acceptDelivery(deliveryId, courierId);
        return "redirect:/courier/my-deliveries?courierId=" + courierId;
    }

    // Page 2: My deliveries (assigned/in-progress)
    @GetMapping("/my-deliveries")
    public String myDeliveries(@RequestParam(defaultValue = "1") Long courierId, Model model) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByCourier(courierId);
        model.addAttribute("deliveries", deliveries);
        model.addAttribute("courierId", courierId);
        model.addAttribute("couriers", courierRepository.findAll());
        return "courier/my-deliveries";
    }

    @PostMapping("/pickup/{deliveryId}")
    public String pickUp(@PathVariable Long deliveryId, @RequestParam Long courierId) {
        deliveryService.pickUpDelivery(deliveryId);
        return "redirect:/courier/my-deliveries?courierId=" + courierId;
    }

    @PostMapping("/deliver/{deliveryId}")
    public String deliver(@PathVariable Long deliveryId, @RequestParam Long courierId) {
        deliveryService.completeDelivery(deliveryId);
        return "redirect:/courier/my-deliveries?courierId=" + courierId;
    }
}
