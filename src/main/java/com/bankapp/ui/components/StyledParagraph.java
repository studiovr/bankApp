package com.bankapp.ui.components;

import com.vaadin.flow.component.html.Paragraph;

public class StyledParagraph extends Paragraph {
    public StyledParagraph(String text) {
        super(text);
        getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");
    }
}