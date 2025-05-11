
## ✅ Opis

Program:
- wczytuje zamówienia i metody płatności z plików JSON,
- dla każdego zamówienia wybiera taki sposób płatności, aby **zmaksymalizować rabat**,
- preferuje użycie punktów lojalnościowych, jeśli nie zmniejsza to rabatu,
- uwzględnia ograniczenia promocji (np. tylko pełna płatność kartą, min. 10% punktami),
- wypisuje **łączną wartość wydatków z podziałem na metody płatności**.

---

## 🛠 Wymagania

- Java 17 lub 21
- Maven (do budowania projektu)

---

## 🚀 Kompilacja

Aby zbudować aplikację jako fat-jar (z zależnościami), użyj:

mvn clean package

---

▶️ Uruchomienie

 java -jar target/Online_payments-1.0-SNAPSHOT.jar /ścieżka/do/orders.json /ścieżka/do/paymentmethods.json

 Pliki json znajdują się w folderze resources

 ---

 🧪 Testy

 W celu włączenia testów należy zastosować komendę:

 mvn test
