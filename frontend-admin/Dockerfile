FROM --platform=$BUILDPLATFORM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
