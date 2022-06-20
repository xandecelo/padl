FROM padlbase:latest

WORKDIR ${PADLBRIDGE_HOME}
COPY ldap/ ${PADLBRIDGE_HOME}

# Java app build time
## TODO: (to decrease size, change build to an intermediate image)
COPY padlapp ${PADLBRIDGE_HOME}/source
RUN chown -R padl:padl ${PADLBRIDGE_HOME}

USER padl
RUN find ${PADLBRIDGE_HOME} -type f -name "*.sh" -exec chmod u+x {} \;

WORKDIR ${PADLBRIDGE_HOME}/source

RUN echo "Building..."; ./gradlew --no-daemon --quiet build ; \
    cp ./app/build/distributions/app.tar ${PADLBRIDGE_HOME}; cd ${PADLBRIDGE_HOME}; \
    tar -xf app.tar ; rm -rf ${PADLBRIDGE_HOME}/source app.tar; \
    echo "Done."

WORKDIR ${PADLBRIDGE_HOME}

ENTRYPOINT [ "/bin/bash", "-c" ]
CMD [ "${PADLBRIDGE_HOME}/run.sh" ]
