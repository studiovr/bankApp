package com.bankapp.ui.client;

import com.bankapp.model.Client;
import com.bankapp.service.ClientService;
import com.bankapp.ui.components.StyledParagraph;
import com.bankapp.utils.MessageProvider;
import com.bankapp.utils.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractClientForm extends VerticalLayout {

    protected final ClientService clientService;
    protected final Binder<Client> binder = new Binder<>(Client.class);
    protected static final Logger logger = LoggerFactory.getLogger(AbstractClientForm.class);

    protected final TextField fullName = new TextField(MessageProvider.getMessage("field.fullName"));
    protected final TextField phoneNumber = new TextField(MessageProvider.getMessage("field.phoneNumber"));
    protected final TextField inn = new TextField(MessageProvider.getMessage("field.inn"));
    protected final TextField address = new TextField(MessageProvider.getMessage("field.address"));

    protected final MemoryBuffer buffer = new MemoryBuffer();
    protected final Upload upload = new Upload(buffer);
    protected byte[] uploadedFile;
    protected Image uploadedImage;

    protected static final String IMAGE_MAX_WIDTH = "300px";
    protected static final String IMAGE_MAX_HEIGHT = "300px";
    protected static final String BUTTON_WIDTH = "200px";

    public AbstractClientForm() {
        this.clientService = ServiceLocator.get(ClientService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    protected void initForm() {
        addClassName("client-form");

        StyledParagraph title = new StyledParagraph(getFormTitle());
        add(title);

        initUpload();

        FormLayout formLayout = new FormLayout();
        formLayout.add(fullName, phoneNumber, inn, address);

        Button saveButton = new Button(MessageProvider.getMessage("button.save"), event -> saveClient());
        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"), event -> getUI()
                .ifPresent(ui -> ui.navigate("")));

        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        saveButton.setWidth(BUTTON_WIDTH);
        cancelButton.setWidth(BUTTON_WIDTH);

        HorizontalLayout uploadAndImageLayout = new HorizontalLayout(upload, uploadedImage);
        uploadAndImageLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        uploadAndImageLayout.setAlignItems(Alignment.CENTER);

        buttonsLayout.add(uploadAndImageLayout, saveButton, cancelButton);
        add(formLayout, buttonsLayout);

        binder.bindInstanceFields(this);
        ClientFormBinderConfigurer.configureBinder(binder, this);
    }

    protected void initUpload() {
        upload.setAcceptedFileTypes(MessageProvider.getMessage("upload.image.types").split(", "));
        upload.addSucceededListener(event -> {
            try {
                uploadedFile = buffer.getInputStream().readAllBytes();
                showUploadedImage(uploadedFile);
            } catch (IOException e) {
                logger.error(MessageProvider.getMessage("upload.image.error"), e);
                Notification.show(MessageProvider.getMessage("upload.image.error") + ": " + e.getMessage());
            }
        });

        upload.addFileRemovedListener(event -> {
            uploadedFile = null;
            uploadedImage.setVisible(false);
        });

        uploadedImage = new Image();
        uploadedImage.setMaxWidth(IMAGE_MAX_WIDTH);
        uploadedImage.setMaxHeight(IMAGE_MAX_HEIGHT);
        uploadedImage.setAlt(MessageProvider.getMessage("upload.image.alt"));
        uploadedImage.setVisible(false);
    }

    protected void showUploadedImage(byte[] imageData) {
        if (uploadedImage != null) {
            StreamResource resource = new StreamResource("uploaded_image", () -> new java.io.ByteArrayInputStream(imageData));
            uploadedImage.setSrc(resource);
            uploadedImage.setVisible(true);
        }
    }

    protected abstract String getFormTitle();

    protected abstract void saveClient();
}