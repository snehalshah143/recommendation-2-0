# Command Performance Optimization Guide

## Quick Commands to Use

### Maven Commands (Faster)
```bash
# Use parallel builds (4 threads)
mvn clean compile -T 4C

# Skip tests for faster builds
mvn clean package -DskipTests

# Use the optimized settings
mvn clean package -s settings.xml

# Fast build without tests and docs
mvn clean package -DskipTests -Dmaven.javadoc.skip=true
```

### NPM Commands (Faster)
```bash
# Use npm ci for faster installs
npm ci

# Use the fast install script
npm run install:fast

# Use fast build
npm run build:fast
```

## System Optimizations

### 1. Windows Defender Exclusions
Add these folders to Windows Defender exclusions:
- `D:\SnehalImp\git\recommendation-2-0\`
- `D:\SnehalImp\git\marketdata\`
- `%USERPROFILE%\.m2\repository`
- `%USERPROFILE%\AppData\Local\npm-cache`

### 2. Environment Variables
Set these environment variables:
```cmd
set MAVEN_OPTS=-Xmx2048m -XX:+UseG1GC
set NODE_OPTIONS=--max-old-space-size=4096
```

### 3. Maven Local Repository
Move Maven repository to SSD:
```cmd
mvn -Dmaven.repo.local=D:\maven-repo clean compile
```

### 4. NPM Cache Optimization
```bash
# Clear and optimize npm cache
npm cache clean --force
npm cache verify

# Use npm ci instead of npm install
npm ci
```

## Performance Monitoring

### Check System Resources
```cmd
# Check disk usage
dir /s

# Check memory usage
tasklist /fi "imagename eq java.exe"
tasklist /fi "imagename eq node.exe"
```

### Maven Performance
```bash
# Enable Maven debug output
mvn clean compile -X

# Check dependency resolution time
mvn dependency:tree -Dverbose
```

## Recommended Hardware Upgrades

1. **SSD Storage**: Move project to SSD for 3-5x faster I/O
2. **More RAM**: 16GB+ recommended for large projects
3. **Faster CPU**: Multi-core processor helps with parallel builds

## IDE Optimizations

### IntelliJ IDEA
- Enable "Build project automatically"
- Increase heap size: Help → Edit Custom VM Options
- Add `-Xmx4g` to increase memory

### VS Code
- Disable unnecessary extensions
- Use workspace-specific settings
- Enable TypeScript incremental compilation




