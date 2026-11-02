FROM grahamdumpleton/mod-wsgi-docker:python-2.7

COPY . /var/www/risk/
COPY ./risk.apache2-conf /usr/local/apache/conf/sites-enabled/000-default.conf




############## APACHE CONFIG ############

# this does not seem to work outside of ubuntu
# RUN a2enmod rewrite
# RUN apt install -y libapache2-mod-wsgi

# same as "a2enmod rewrite" and libapache2-mod-wsgi install
RUN sed -i '/LoadModule rewrite_module/s/^#//g' /usr/local/apache/conf/httpd.conf
RUN mod_wsgi-express module-config >> /usr/local/apache/conf/httpd.conf

# add same allowing of /var/www as ubuntu has
RUN echo "<Directory /var/www/>\n\
        Options Indexes FollowSymLinks\n\
        AllowOverride None\n\
        Require all granted\n\
</Directory>" >> /usr/local/apache/conf/httpd.conf

# add same reading of sites-enabled config as ubuntu
RUN echo "\n# Include the virtual host configurations:\n\
IncludeOptional conf/sites-enabled/*.conf\n" >> /usr/local/apache/conf/httpd.conf

# in our site conf file we point to apache2 dir as thats what ubuntu uses
RUN ln -s /usr/local/apache/logs /var/log/apache2




############## PYTHON SETUP ############

RUN apt-get update
RUN pip2 install Django==1.2.7 --no-cache-dir --no-binary :all:
RUN pip2 install -r /var/www/risk/requirements.txt
RUN chmod a+rw /var/www/risk/database /var/www/risk/storage
RUN rm /var/www/risk/static/admin
RUN ln -s /usr/local/python/lib/python2.7/site-packages/django/contrib/admin/media /var/www/risk/static/admin





############## RUN APACHE ############

EXPOSE 80

CMD httpd -DFOREGROUND

