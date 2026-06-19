# bear

Bot z interfejsem (Java Swing).

## Pobierz i zainstaluj (Linux / Ubuntu)

**[⬇ Pobierz bear_1.0-1_amd64.deb](https://github.com/batlindev/task1/releases/download/v1.0/bear_1.0-1_amd64.deb)**

```bash
sudo dpkg -i bear_1.0-1_amd64.deb
```

Potem uruchom **bear** z menu aplikacji (albo komenda `bear` w terminalu).

Java jest spakowana w środku — nic więcej instalować nie trzeba.

Odinstalowanie:

```bash
sudo dpkg -r bear
```

## Konfiguracja

Token Telegrama i chat_id wpisujesz w panelu Bot Control — startują puste, nie z pliku. Zapisz je w presecie, żeby przetrwały restart.

## Build ze źródeł (dla developera)

Wymaga JDK 17.

```bash
mvn package          # buduje jar do target/
```

Zbudowanie nowego `.deb`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package
jpackage --type deb --name bear --app-version 1.0 \
  --input target --main-jar demo-1.0-SNAPSHOT.jar \
  --main-class com.example.Main --dest dist \
  --linux-shortcut --vendor batlin
```
