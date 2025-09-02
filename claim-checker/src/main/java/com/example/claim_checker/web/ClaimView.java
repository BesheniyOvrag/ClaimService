package com.example.claim_checker.web;

import com.example.claim_checker.entity.Claim;
import com.example.claim_checker.exception.InvalidUserClaimException;
import com.example.claim_checker.model.ClaimRequest;
import com.example.claim_checker.model.ClaimResponse;
import com.example.claim_checker.service.ClaimCheckerService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@Route("")
public class ClaimView extends VerticalLayout {

    private final ClaimCheckerService claimCheckerService;
    private final Grid<Claim> grid = new Grid<>(Claim.class, false);
    private final Text decisionResult = new Text("");

    public ClaimView(ClaimCheckerService claimCheckerService) {
        this.claimCheckerService = claimCheckerService;

        Select<String> policyType = new Select<>();
        policyType.setLabel("Policy Type");
        policyType.setItems("Premium", "Standard", "Economy");
        policyType.setMaxWidth("200px");

        TextField name = new TextField("Name");
        name.setMaxWidth("150px");
        name.setClearButtonVisible(true);

        TextField surname = new TextField("Surname");
        surname.setMaxWidth("150px");
        surname.setClearButtonVisible(true);

        EmailField emailField = new EmailField();
        emailField.setLabel("email");
        emailField.setMaxWidth("250px");
        emailField.setClearButtonVisible(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());

        DatePicker datePicker = new DatePicker("Date");
        datePicker.setMaxWidth("200px");

        TextArea description = new TextArea("Description");
        description.setMaxWidth("700px");
        description.setHeight("200px");
        description.setClearButtonVisible(true);

        Button sendButton = new Button("Send");
        sendButton.setMaxWidth("100px");

        sendButton.addClickListener(event -> {
            try {
                String type = policyType.getValue();
                LocalDate date = datePicker.getValue();
                String desc = description.getValue();
                String nameString = name.getValue();
                String surnameString = surname.getValue();
                String emailString = emailField.getValue();

                validateInput(desc, date, type, nameString, surnameString, emailString);

                ClaimRequest request = new ClaimRequest(type, nameString, surnameString, emailString, date.toString(), desc);

                ClaimResponse response = claimCheckerService.checkClaim(request);

                decisionResult.setText("Decision: " + response.getDecision());

                refreshGrid();

                description.clear();

            } catch (InvalidUserClaimException ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Unexpected error " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });

        FormLayout form = new FormLayout(policyType, name, surname, emailField, datePicker, description, sendButton);

        configureGrid();

        add(form, decisionResult, grid);

        refreshGrid();
    }

    private void validateInput(String desc, LocalDate date, String type, String name, String surname, String email) {
        if (desc == null || desc.trim().isEmpty()) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Description is empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Policy type is empty");
        }
        if (date == null) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Date is empty");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Date cannot be in the future");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Name is empty");
        }
        if (surname == null || surname.trim().isEmpty()) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Surname is empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "email is empty");
        }
    }

    private void configureGrid() {
        grid.addColumn(Claim::getId).setHeader("ID").setWidth("10px");
        grid.addColumn(claim -> {
            Object pt = claim.getPolicyType();
            return pt == null ? "" : pt.toString();
        }).setHeader("Policy Type").setWidth("80px");
        grid.addColumn(Claim::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Claim::getSurname).setHeader("Surname").setAutoWidth(true);
        grid.addColumn(Claim::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Claim::getClaimDate).setHeader("Date").setWidth("70px");
        grid.addColumn(new ComponentRenderer<>(claim -> {
            Span span = new Span(claim.getDescription());
            span.getStyle().set("white-space", "normal");
            span.getStyle().set("word-wrap", "break-word");
            return span;
        })).setHeader("Description").setFlexGrow(1).setResizable(true);
        grid.addColumn(new ComponentRenderer<>(claim -> {
            Span span = new Span(claim.getDecision());
            span.getStyle().set("white-space", "normal");
            span.getStyle().set("word-wrap", "break-word");
            return span;
        })).setHeader("Decision").setWidth("300px").setResizable(true);
        grid.setAllRowsVisible(true);
        grid.setHeight("500px");
        grid.getColumns().forEach(col -> col.setResizable(true));
        grid.addColumn(Claim::getBooleanDecision).setHeader("Status").setWidth("50px").setSortable(true);
    }

    private void refreshGrid() {
        List<Claim> items = claimCheckerService.getAllClaims();
        grid.setItems(items);
    }
}
