FROM nginx:1.27.3-perl

COPY dist /usr/share/nginx/html
COPY resources/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80/tcp

CMD ["/usr/sbin/nginx", "-g", "daemon off;"]
