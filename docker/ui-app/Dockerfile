FROM node:10

WORKDIR /usr/src/app
COPY ui/package*.json ./

RUN npm install
COPY . .

EXPOSE 8080 1337
CMD [ "node", "ui/server.js" ]