

### Alfresco SDK Setup

- http://docs.alfresco.com/5.1/tasks/alfresco-sdk-tutorials-amp-archetype.html
- http://docs.alfresco.com/sdk2.1/tasks/alfresco-sdk-rad-intellij-hot-reloading.html


### Create Project

```
mvn archetype:generate -Dfilter=org.alfresco:
```

### Select AMPs (2)

```
2: remote -> org.alfresco.maven.archetype:alfresco-amp-archetype (Sample project with full support for lifecycle and rapid development of Repository AMPs (Alfresco Module Packages))
```

### Run

```
./run.sh
```

### Note

- Option 2 จะไม่มี Share ติดมาทำให้ไม่สามารถเข้า Repository ได้
- แก้โดยสร้างโปรเจค แบบ (3) สั่งรันแล้วเข้าผ่าน `http://localhost:8081/share`
