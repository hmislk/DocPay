# DocPay

Standalone JavaFX desktop app to manage doctor payments (Galle Cooperative Hospital).

Stack
- Java 11 + Maven
- JavaFX UI
- Embedded H2 (file) with JPA (EclipseLink)
- BCrypt password hashing

Database
- File: %USERPROFILE%/AppData/DocPay/db.mv.db
- Auto schema: create-or-extend via JPA

Dev Run
- Prereq: JDK 11+, Maven installed (`mvn` on PATH)
- Command: `mvn -Dprism.order=sw javafx:run`
- First run: set an admin username/password in Login screen; click Login to create admin; then login again.

Packaging
- Shade builds a fat jar on `mvn package`. jpackage config can be added later for Windows installer.

Notes
- Entities defined: User, Doctor, Patient, Appointment, DoctorPayment, ReceiptSequence
- Minimal login flow implemented; further screens TBD.
