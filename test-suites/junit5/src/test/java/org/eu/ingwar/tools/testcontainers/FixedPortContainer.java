package org.eu.ingwar.tools.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.utility.DockerImageName;

/**
 *
 * @author lprimak
 * @param <SELF>
 */
public class FixedPortContainer<SELF extends FixedPortContainer<SELF>> extends GenericContainer<SELF> {

    public FixedPortContainer(DockerImageName imageName) {
        super(imageName);
    }

    /**
     * Bind a fixed TCP port on the docker host to a container port
     *
     * @param hostPort a port on the docker host, which must be available
     * @param containerPort a port in the container
     * @return this container
     */
    public SELF withFixedExposedPort(int hostPort, int containerPort) {

        return withFixedExposedPort(hostPort, containerPort, InternetProtocol.TCP);
    }

    /**
     * Bind a fixed port on the docker host to a container port
     *
     * @param hostPort a port on the docker host, which must be available
     * @param containerPort a port in the container
     * @param protocol an internet protocol (tcp or udp)
     * @return this container
     */
    public SELF withFixedExposedPort(int hostPort, int containerPort, InternetProtocol protocol) {

        super.addFixedExposedPort(hostPort, containerPort, protocol);

        return self();
    }

    private static final long serialVersionUID = 1L;
}
