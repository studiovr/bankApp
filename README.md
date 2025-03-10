# Тестовый проект для банковского приложения на Vaadin

Чтобы получить доступ к проекту напрямую из GitHub, клонируйте репозиторий и импортируйте проект в вашу IDE как Maven-проект. Убедитесь, что у вас установлена Java 17.

Запустите приложение с помощью команды `mvn jetty:run` и откройте [http://localhost:8080](http://localhost:8080) в браузере.

Для запуска приложения в режиме production используйте команду `mvn jetty:run -Pproduction`.

## Структура проекта

Проект следует стандартной структуре каталогов Maven:

### 1. Корневая директория проекта

- **`pom.xml`** — Maven-файл для управления зависимостями и сборкой проекта.
- **`frontend/`** — клиентская часть приложения (стили, темы, конфигурации).
- **`src/`** — основная директория для исходного кода и ресурсов.

### 2. `src/main/java` — Исходный код приложения

#### Пакет `com.bankapp`

- **`model/`** — Модели данных:
  - `Client.java` — сущность клиента (ФИО, номер телефона, ИНН, адрес, скан паспорта).
  - `Account.java` — сущность счета (номер счета, баланс, статус, БИК, валюта).
  - `Transaction.java` — сущность транзакции (тип, сумма, валюта, счета отправителя и получателя).

- **`enums/`** — Перечисления:
  - `AccountStatus.java` — статусы счета (Открыт, Закрыт).
  - `Currency.java` — валюты (рубли, доллары, евро).
  - `TransactionType.java` — типы транзакций (перевод, зачисление).

- **`repository/`** — Репозитории для работы с базой данных:
  - `ClientRepository.java` — методы для работы с клиентами.
  - `AccountRepository.java` — методы для работы со счетами.
  - `TransactionRepository.java` — методы для работы с транзакциями.

- **`service/`** — Сервисы для бизнес-логики:
  - `ClientService.java` — управление клиентами (создание, редактирование, удаление).
  - `AccountService.java` — управление счетами (создание, закрытие, переводы, зачисления).
  - `TransactionService.java` — обработка транзакций.

- **`ui/`** — Пользовательский интерфейс (Vaadin):
  - `MainView.java` — главное представление приложения.
  - `client/` — компоненты для работы с клиентами:
    - `ClientForm.java` — форма для создания/редактирования клиента.
    - `ClientListView.java` — список всех клиентов.
  - `account/` — компоненты для работы со счетами:
    - `AccountForm.java` — форма для создания/редактирования счета.
    - `AccountListView.java` — список всех счетов.
  - `transaction/` — компоненты для работы с транзакциями:
    - `DepositForm.java` — форма для зачисления средств.
    - `TransferForm.java` — форма для перевода средств.
    - `TransactionListView.java` — список всех транзакций.

- **`utils/`** — Вспомогательные классы:
  - `DatabaseUtil.java` — утилиты для работы с базой данных.
  - `DiContainer.java` — простой Dependency Injection контейнер.
  - `TransactionManager.java` — управление транзакциями (начало, коммит, откат).

- **`AppShell.java`** — конфигурация приложения (например, для PWA).
- **`MainView.java`** — главное представление приложения.

### 3. `src/test/java` — Тесты

- **`service/`** — Юнит-тесты для сервисов:
  - `ClientServiceTest.java` — тесты для `ClientService`.
  - `AccountServiceTest.java` — тесты для `AccountService`.
  - `TransactionServiceTest.java` — тесты для `TransactionService`.