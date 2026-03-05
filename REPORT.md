# รายงานโครงงาน Gun Mayhem Arena

## 1) ข้อมูลโครงงาน

- ชื่อโครงงาน: Gun Mayhem Arena
- ภาษา: Java (JavaFX)
- ระบบ Build: Gradle

## 2) วิธีใช้งานโปรแกรม

### 2.1 เตรียมสภาพแวดล้อม

- ติดตั้ง JDK (แนะนำเวอร์ชัน 21 หรือสูงกว่า)
- ใช้งานในโฟลเดอร์โปรเจกต์นี้ (`Project`)

### 2.2 คำสั่งรันโปรแกรม

```powershell
.\gradlew.bat run
```

### 2.3 คำสั่งทดสอบ

```powershell
.\gradlew.bat test
```

### 2.4 ปุ่มควบคุมในเกม

- Player 1: `A/D` เดิน, `W` กระโดด, `S` ลงแพลตฟอร์ม, `SPACE` โจมตี/ยิง
- Player 2: `LEFT/RIGHT` เดิน, `UP` กระโดด, `DOWN` ลงแพลตฟอร์ม, `ENTER` โจมตี/ยิง
- ทั่วไป: `ESC` พักเกม, `F11` หรือ `ALT+ENTER` สลับ Fullscreen

## 3) เอกสารอธิบายโค้ด (JavaDoc)

> ตามเงื่อนไขรายงาน: วางลิงก์ JavaDoc หลังวิธีใช้งานโปรแกรม

### 3.1 คำสั่งสร้าง JavaDoc

```powershell
.\gradlew.bat javadoc
```

### 3.2 ลิงก์ JavaDoc

- [JavaDoc - หน้าแรก](build/docs/javadoc/index.html)
- [JavaDoc - สรุปแพ็กเกจ](build/docs/javadoc/overview-summary.html)

### 3.3 ไฟล์โปรแกรมแบบ `.jar`

### 3.3.1 คำสั่งสร้างไฟล์ `.jar`

```powershell
.\gradlew.bat jar
```

### 3.3.2 ลิงก์ไฟล์ `.jar`

- [Project-1.0-SNAPSHOT.jar](build/libs/Project-1.0-SNAPSHOT.jar)

### 3.4 คำสั่งเตรียมไฟล์ก่อน push

```powershell
.\gradlew.bat javadoc jar
git add build/docs/javadoc build/libs/Project-1.0-SNAPSHOT.jar
```

## 4) UML Diagram (แยกรูปครบทุกส่วน)

### 4.1 Config

![Config UML](src/main/resources/puml/Config.png)

### 4.2 Entity

![Entity UML](src/main/resources/puml/Entity.png)

### 4.3 Entity-Weapon

![Entity-Weapon UML](src/main/resources/puml/Entity-Weapon.png)

### 4.4 GameMain

![GameMain UML](src/main/resources/puml/GameMain.png)

### 4.5 Logic

![Logic UML](src/main/resources/puml/Logic.png)

### 4.6 Map

![Map UML](src/main/resources/puml/Map.png)

### 4.7 UI

![UI UML](src/main/resources/puml/UI.png)

> ไฟล์ต้นฉบับ UML (`.puml`) อยู่ที่ root ของโปรเจกต์  
> (`Config.puml`, `Entity.puml`, `Entity-Weapon.puml`, `GameMain.puml`, `Logic.puml`, `Map.puml`, `UI.puml`)

## 5) สรุปโครงสร้างโค้ดโดยย่อ

- `src/main/java/game/GameMain.java`: จุดเริ่มต้นโปรแกรมและการเปลี่ยนหน้าจอ
- `src/main/java/game/config/`: ค่าคงที่เกม (`GameSettings`)
- `src/main/java/game/entities/`: ผู้เล่น กระสุน อาวุธ กับดัก พาวเวอร์อัป
- `src/main/java/game/logic/`: กล้อง เสียง ตัวจับเวลา และ interface หลัก
- `src/main/java/game/map/`: ข้อมูลแมพ พื้นผิว และการเรนเดอร์แมพ
- `src/main/java/game/ui/`: เมนู หน้าเลือกแมพ/อาวุธ หน้าจอเล่น และ pause overlay

## 6) หมายเหตุ

- หากเปิดลิงก์ JavaDoc ไม่ได้จากตัว Markdown ให้เปิดไฟล์นี้โดยตรง:
  - `build/docs/javadoc/index.html`
- ไฟล์รายงานนี้อ้างอิงลิงก์แบบ relative path ดังนั้นต้อง push โฟลเดอร์ `build/docs/javadoc` และไฟล์ `build/libs/Project-1.0-SNAPSHOT.jar` ขึ้น repository ด้วย
- ควรรัน `.\gradlew.bat javadoc` ใหม่ทุกครั้งหลังแก้โค้ด เพื่อให้เอกสารอัปเดตล่าสุด
