ant dist -Dno.test=true

mvn deploy:deploy-file -Dfile=mysql-connector-mxj-gpl-5-0-12.jar -DrepositoryId=vidal-3rd-party-releases -DgroupId=mysql -DartifactId=mysql-connector-mxj-gpl -Dversion=5.0.12-vidal -Durl=http://nexus.vidal.net/content/repositories/vidal-3rd-party-releases/

mvn deploy:deploy-file -Dfile=mysql-connector-mxj-gpl-5-0-12-db-files.jar -DrepositoryId=vidal-3rd-party-releases -DgroupId=mysql -DartifactId=mysql-connector-mxj-gpl-db-files -Dversion=5.0.12-vidal -Dclassifier=files -Durl=http://nexus.vidal.net/content/repositories/vidal-3rd-party-releases/

yum install libaio.i686
