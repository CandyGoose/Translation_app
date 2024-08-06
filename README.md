# Translation service
## Описание
Веб-приложение для перевода текста с одного языка на другой, использующее Yandex Translate API. 

## Стек технологий
- Java 17
- Spring Boot
- Maven
- PostgreSQL 
- Yandex Translate API
- JUnit 5

## Запуск
1. Склонируйте репозиторий:
```
git clone https://github.com/CandyGoose/Translation_app
cd Translation_app
```
2. Получите [Yandex Translate API](https://yandex.cloud/ru/docs/translate/quickstart)
3. Измените файл `.env` в корне проекта
4. Запустите приложение и базу данных с помощью Docker Compose:
```
docker-compose up --build
```
5. Дождитесь завершения запуска, приложением можно будет воспользоваться:
- при помощи curl:
```
curl -X POST "http://localhost:8080/api/translate?sourceLanguage=en&targetLanguage=ru&text=Hello%20world%2C%20this%20is%20my%20first%20program"
```
- c помощью Postman, отправив POST-запрос на адрес http://localhost:8080/api/translate с JSON:
```json
{
  "sourceLanguage": "en",
  "targetLanguage": "ru",
  "text": "Hello world, this is my first program"
}
```
- через простой фронтенд: http://localhost:8080

<img src="https://github.com/user-attachments/assets/ab62bc5d-6c51-4f33-a0e2-6355b5a72771" alt="Front OK" style="width: 500px; height: auto;">

<img src="https://github.com/user-attachments/assets/ecb45753-caf6-43ea-97ab-4e9f618d8980" alt="Front Err" style="width: 500px; height: auto;">
