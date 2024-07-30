./mvnw  clean package -Pproduction
docker build --platform linux/amd64 -t orf1/gemba-ai-platform .
docker push orf1/gemba-ai-platform:latest