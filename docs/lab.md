# Spring Boot - Lab Setup Guide (Ubuntu)

Update your system before starting:

```bash
sudo apt update && sudo apt upgrade -y
```

---

## 1. Java 17

```bash
sudo apt install -y openjdk-17-jdk
```

Set `JAVA_HOME` — add to `~/.bashrc`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Reload and verify:

```bash
source ~/.bashrc
java -version
echo $JAVA_HOME
```

> If wrong version appears, run: `sudo update-alternatives --config java`

---

## 2. Maven

```bash
sudo apt install -y maven
mvn -version
```

> Confirm `mvn -version` shows **Java version: 17.x.x**

---

## 3. VS Code

```bash
sudo apt install -y software-properties-common apt-transport-https wget
wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | \
  sudo tee /usr/share/keyrings/vscode.gpg > /dev/null
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/vscode.gpg] \
  https://packages.microsoft.com/repos/vscode stable main" | \
  sudo tee /etc/apt/sources.list.d/vscode.list
sudo apt update && sudo apt install -y code
```

Install recommended extensions:

```bash
code --install-extension vscjava.vscode-java-pack
code --install-extension vmware.vscode-boot-dev-pack
```

---

## 4. IntelliJ IDEA (Alternative)

```bash
sudo snap install intellij-idea-community --classic
```

Configure JDK: **File > Project Structure > SDKs > + > Add JDK** — select `/usr/lib/jvm/java-17-openjdk-amd64`

> Community Edition is free and sufficient for this course.

---

## 5. Docker

```bash
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

Allow running Docker without `sudo`:

```bash
sudo usermod -aG docker $USER
```

> Log out and log back in, then test:

```bash
docker run hello-world
```

---

## Verification Checklist

| Command | Expected Output |
|---------|----------------|
| `java -version` | openjdk 17.x.x |
| `echo $JAVA_HOME` | /usr/lib/jvm/java-17-openjdk-amd64 |
| `mvn -version` | Apache Maven 3.x.x, Java 17 |
| `code --version` | version number |
| `docker --version` | Docker version 2x.x.x |
| `docker compose version` | Docker Compose v2.x.x |

All good? You are ready for the Spring Boot labs!
