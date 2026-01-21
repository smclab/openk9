FROM node:18-alpine

RUN apk add --no-cache docker-cli

# Set working directory
WORKDIR /app

# Copy the script
COPY seed.js .

# Run the script
CMD ["node", "seed.js"]
