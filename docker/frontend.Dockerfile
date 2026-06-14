FROM docker.io/library/node:18-alpine AS build
ENV TZ=Asia/Shanghai
WORKDIR /app

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    npm config set registry https://registry.npmmirror.com

COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps --no-audit --no-fund

COPY frontend/ .
RUN npm run build

FROM docker.io/library/nginx:alpine
ENV TZ=Asia/Shanghai
WORKDIR /etc/nginx

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
  CMD wget --quiet --tries=1 --spider http://localhost/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
