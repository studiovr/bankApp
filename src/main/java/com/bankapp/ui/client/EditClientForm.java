package com.bankapp.ui.client;

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
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.model.Client;
import com.bankapp.service.ClientService;
import com.bankapp.utils.DIContainer;

import java.io.IOException;

@Route("edit-client")
public class EditClientForm extends VerticalLayout implements HasUrlParameter<Long> {

    private final ClientService clientService;
    private final Binder<Client> binder = new Binder<>(Client.class);
    private static final Logger logger = LoggerFactory.getLogger(EditClientForm.class);


    private TextField idField = new TextField("ID");
    private TextField fullName = new TextField("ФИО");
    private TextField phoneNumber = new TextField("Номер телефона");
    private TextField inn = new TextField("ИНН");
    private TextField address = new TextField("Адрес");
    private TextField createdAtField = new TextField("Дата создания");
    private TextField updatedAtField = new TextField("Дата обновления");
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private byte[] uploadedFile;
    private Image uploadedImage;

    private Client client;
    private boolean isEditing = false;
    private String buttonText = "Остановить редактирование";

    private Button editButton;

    public EditClientForm() {
        this.clientService = DIContainer.get(ClientService.class);
        initForm();
    }

    private void initForm() {
        setAlignItems(Alignment.CENTER);

        addClassName("deposit-form");
        var title = new com.vaadin.flow.component.html.Paragraph("Форма редактирования клиента");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");

        add(title);

        binder.forField(fullName)
                .asRequired("ФИО обязательно")
                .withValidator(new StringLengthValidator(
                        "ФИО должно содержать минимум 3 символа", 3, null))
                .bind(Client::getFullName, Client::setFullName);

        binder.forField(phoneNumber)
                .asRequired("Номер телефона обязателен")
                .withValidator(new RegexpValidator(
                        "Номер телефона должен быть в формате +7XXXXXXXXXX", "^\\+7\\d{10}$"))
                .bind(Client::getPhoneNumber, Client::setPhoneNumber);

        binder.forField(inn)
                .asRequired("ИНН обязателен")
                .withValidator(new RegexpValidator(
                        "ИНН должен состоять из 12 цифр", "^\\d{12}$"))
                .bind(Client::getInn, Client::setInn);

        binder.forField(address)
                .asRequired("Адрес обязателен")
                .bind(Client::getAddress, Client::setAddress);

        FormLayout formLayout = new FormLayout();

        idField.setReadOnly(true);
        createdAtField.setReadOnly(true);
        updatedAtField.setReadOnly(true);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf");
        upload.addSucceededListener(event -> {
            try {
                uploadedFile = buffer.getInputStream().readAllBytes();
                showUploadedImage(uploadedFile);
            } catch (IOException e) {
                Notification.show("Ошибка при загрузке файла: " + e.getMessage());
                logger.error("Ошибка при загрузке файла: {}", e.getMessage());
            }
        });

        upload.addFileRemovedListener(event -> {
            uploadedFile = null;
            uploadedImage.setVisible(false);
        });

        uploadedImage = new Image();
        uploadedImage.setMaxWidth("300px");
        uploadedImage.setMaxHeight("300px");
        uploadedImage.setAlt("Загруженное изображение");
        uploadedImage.setVisible(false);

        HorizontalLayout uploadAndImageLayout = new HorizontalLayout(upload, uploadedImage);
        uploadAndImageLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        uploadAndImageLayout.setAlignItems(Alignment.CENTER);

        formLayout.add(idField, fullName, phoneNumber, inn, address, createdAtField, uploadAndImageLayout);

        editButton = new Button(buttonText, event -> toggleEditingMode());
        Button saveButton = new Button("Сохранить", event -> saveClient());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(true);

        buttonsLayout.setAlignItems(Alignment.CENTER);

        saveButton.setWidth("200px");
        cancelButton.setWidth("200px");

        uploadAndImageLayout.setWidth("100%");

        buttonsLayout.add(uploadAndImageLayout, editButton, saveButton, cancelButton);

        add(formLayout, buttonsLayout);

        binder.bindInstanceFields(this);
        toggleEditingMode();
    }

    private void showUploadedImage(byte[] imageData) {
        if (uploadedImage != null) {
            StreamResource resource = new StreamResource("uploaded_image", () -> new java.io.ByteArrayInputStream(imageData));
            uploadedImage.setSrc(resource);
            uploadedImage.setVisible(true);
        }
    }

    private void toggleEditingMode() {
        isEditing = !isEditing;
        fullName.setReadOnly(!isEditing);
        phoneNumber.setReadOnly(!isEditing);
        inn.setReadOnly(!isEditing);
        address.setReadOnly(!isEditing);
        upload.setEnabled(isEditing);

        if (isEditing) {
            buttonText = "Остановить редактирование";
        } else {
            buttonText = "Включить редактирование";
        }
        editButton.setText(buttonText);
    }

    private void saveClient() {
        if (binder.writeBeanIfValid(client)) {
            if (uploadedFile != null) {
                client.setPassportScanCopy(uploadedFile);
            }
            try {
                clientService.updateClient(client);
                logger.info("Клиент успешно обновлен: {}", client.getId());
                Notification.show("Клиент успешно обновлен");
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                logger.error("Ошибка при обновлении клиента: {}", e.getMessage());
                Notification.show("Ошибка: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != null) {
            try {
                client = clientService.findClientById(parameter);
                binder.readBean(client);

                idField.setValue(client.getId().toString());
                createdAtField.setValue(client.getCreatedAt().toString());
                updatedAtField.setValue(client.getUpdatedAt().toString());

                if (client.getPassportScanCopy() != null) {
                    uploadedFile = client.getPassportScanCopy();
                    showUploadedImage(uploadedFile);
                }
            } catch (Exception e) {
                logger.error("Ошибка при загрузке клиента: {}", e.getMessage());
                Notification.show("Ошибка при загрузке клиента: " + e.getMessage());
            }
        }
    }
}