FROM tomcat:8

# add server config file
COPY infra/tomcat/server.xml /usr/local/tomcat/conf/

# add admin user
# COPY infra/tomcat/tomcat-users.xml /usr/local/tomcat/conf/

# allow access to manager application from remote hosts
# COPY infra/tomcat/context.xml /usr/local/tomcat/webapps/manager/META-INF/

# copy static content
COPY toderita-1359 /usr/local/tomcat/webapps/
