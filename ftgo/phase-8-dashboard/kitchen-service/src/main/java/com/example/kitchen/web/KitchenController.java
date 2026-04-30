package com.example.kitchen.web;

import com.example.kitchen.service.KitchenTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenTicketService kitchenTicketService;

    @GetMapping("/{restaurantId}/orders")
    public String viewOrders(@PathVariable Long restaurantId, Model model) {
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("tickets", kitchenTicketService.getTicketsByRestaurant(restaurantId));
        return "kitchen/orders";
    }

    @PostMapping("/tickets/{ticketId}/accept")
    public String acceptTicket(@PathVariable Long ticketId, @RequestParam Long restaurantId) {
        kitchenTicketService.acceptTicket(ticketId);
        return "redirect:/kitchen/" + restaurantId + "/orders";
    }

    @PostMapping("/tickets/{ticketId}/prepare")
    public String startPreparing(@PathVariable Long ticketId, @RequestParam Long restaurantId) {
        kitchenTicketService.startPreparing(ticketId);
        return "redirect:/kitchen/" + restaurantId + "/orders";
    }

    @PostMapping("/tickets/{ticketId}/ready")
    public String markReady(@PathVariable Long ticketId, @RequestParam Long restaurantId) {
        kitchenTicketService.markReady(ticketId);
        return "redirect:/kitchen/" + restaurantId + "/orders";
    }
}
