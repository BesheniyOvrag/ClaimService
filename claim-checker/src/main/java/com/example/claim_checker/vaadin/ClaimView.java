package com.example.claim_checker.vaadin;

import com.example.claim_checker.exception.InvalidUserClaimException;
import com.example.claim_checker.model.ClaimRequest;
import com.example.claim_checker.model.ClaimResponse;
import com.example.claim_checker.service.ClaimCheckerService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

@Route("")
public class ClaimView extends VerticalLayout {

    private final ClaimCheckerService claimCheckerService;

    public ClaimView(ClaimCheckerService claimCheckerService) {
        this.claimCheckerService = claimCheckerService;

        Select<String> policyType = new Select<>();
        policyType.setLabel("Policy Type");
        policyType.setItems("Premium", "Standard", "Economy");

        DatePicker datePicker = new DatePicker("Date");

        TextArea description = new TextArea("Description");
        description.setWidthFull();

        Button sendButton = new Button("Send");

        Text decisionResult = new Text("");

        sendButton.addClickListener(event -> {

                String type = policyType.getValue();
                LocalDate date = datePicker.getValue();
                String desc = description.getValue();


                if (desc.isEmpty()){
                    throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Description of the empty");
                }
                if (type.isEmpty()){
                    throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Type of policy is empty");
                }
                if (datePicker.toString().isEmpty()){
                    throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Data is empty");
                }
                try {
                    if (date.isAfter(LocalDate.now())) {
                        throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Data is wrong");
                    }
                }
                catch (Exception e){
                    throw new InvalidUserClaimException(HttpStatus.BAD_REQUEST, "Wrong format of data");
                }

                ClaimRequest request = new ClaimRequest(type, date.toString(), desc);

                ClaimResponse response = claimCheckerService.checkClaim(request);

                decisionResult.setText("Decision: " + response.getDecision());
            });

        FormLayout form = new FormLayout(policyType, datePicker, description, sendButton);
        add(form, decisionResult);
    }
}
