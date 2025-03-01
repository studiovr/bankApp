package com.bankapp.ui.client;

import com.bankapp.binderConfigurer.ClientFormBinderConfigurer;
import com.bankapp.service.ClientService;
import com.bankapp.utils.DIContainer;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.model.Client;

import java.io.IOException;

@Route("client-form")
public class ClientForm extends VerticalLayout {

    private final ClientService clientService;
    private final Binder<Client> binder = new Binder<>(Client.class);
    private static final Logger logger = LoggerFactory.getLogger(EditClientForm.class);


    private final TextField fullName = new TextField("ФИО");
    private final TextField phoneNumber = new TextField("Номер телефона");
    private final TextField inn = new TextField("ИНН");
    private final TextField address = new TextField("Адрес");

    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private byte[] uploadedFile;
    private Image uploadedImage;

    private static final String IMAGE_MAX_WIDTH = "300px";
    private static final String IMAGE_MAX_HEIGHT = "300px";
    private static final String BUTTON_WIDTH = "200px";

    public TextField getFullName() {
        return fullName;
    }

    public TextField getPhoneNumber() {
        return phoneNumber;
    }

    public TextField getInn() {
        return inn;
    }

    public TextField getAddress() {
        return address;
    }

    public ClientForm() {
        this.clientService = DIContainer.get(ClientService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    private void initForm() {
        addClassName("deposit-form");
        var title = new com.vaadin.flow.component.html.Paragraph("Форма создания клиента");
        title.getStyle().set("font-size", "20px").set("font-weight", "bold").set("margin-bottom", "15px");

        add(title);

        initUpload();

        FormLayout formLayout = new FormLayout();
        formLayout.add(fullName, phoneNumber, inn, address);

        Button saveButton = new Button("Сохранить", event -> saveClient());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

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

    private void initUpload() {
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.addSucceededListener(event -> {
            try {
                uploadedFile = buffer.getInputStream().readAllBytes();
                showUploadedImage(uploadedFile);
            } catch (IOException e) {
                logger.error("Ошибка при загрузке файла", e);
                Notification.show("Ошибка при загрузке файла: " + e.getMessage());
            }
        });

        upload.addFileRemovedListener(event -> {
            uploadedFile = null;
            uploadedImage.setVisible(false);
        });

        uploadedImage = new Image();
        uploadedImage.setMaxWidth(IMAGE_MAX_WIDTH);
        uploadedImage.setMaxHeight(IMAGE_MAX_HEIGHT);
        uploadedImage.setAlt("Загруженное изображение");
        uploadedImage.setVisible(false);
    }

    private void showUploadedImage(byte[] imageData) {
        if (uploadedImage != null) {
            StreamResource resource = new StreamResource("uploaded_image", () -> new java.io.ByteArrayInputStream(imageData));
            uploadedImage.setSrc(resource);
            uploadedImage.setVisible(true);
        }
    }

    private void saveClient() {
        Client client = new Client();
        if (binder.writeBeanIfValid(client)) {
            logger.info("Форма валидна. Начинаем сохранение клиента...");
            if (uploadedFile != null) {
                client.setPassportScanCopy(uploadedFile);
            }
            try {
                clientService.createClient(client);
                logger.info("Клиент успешно сохранен: {}", client);
                Notification.show("Клиент успешно сохранен");
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                logger.error("Ошибка при сохранении клиента", e);
                Notification.show("Ошибка: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }
}