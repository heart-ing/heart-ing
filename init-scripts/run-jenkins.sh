docker run --name jenkins -e JENKINS_OPTS="--prefix=/jenkins" -d -p 8081:8080 -p 50000:50000 -v /home/jenkins:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -u root youngmookk/hearting-jenkins