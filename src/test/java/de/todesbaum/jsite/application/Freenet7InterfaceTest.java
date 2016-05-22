package de.todesbaum.jsite.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import de.todesbaum.jsite.application.Freenet7Interface.ClientSupplier;
import de.todesbaum.jsite.application.Freenet7Interface.ConnectionSupplier;
import de.todesbaum.jsite.application.Freenet7Interface.NodeSupplier;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.GenerateSSK;
import de.todesbaum.util.freenet.fcp2.Message;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link Freenet7Interface}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Freenet7InterfaceTest {

	private static final String NODE_ADDRESS = "node-address";
	private static final int NODE_PORT = 12345;
	private static final String IDENTIFIER = "connection-identifier";
	private static final String INSERT_URI = "insert-uri";
	private static final String REQUEST_URI = "request-uri";

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	private final NodeSupplier nodeSupplier = mock(NodeSupplier.class);
	private final ConnectionSupplier connectionSupplier = mock(ConnectionSupplier.class);
	private final ClientSupplier clientSupplier = mock(ClientSupplier.class);
	private final Freenet7Interface freenet7Interface = new Freenet7Interface(nodeSupplier, connectionSupplier, clientSupplier);

	@Test
	public void defaultConstructorCanBeCalled() {
		new Freenet7Interface();
	}

	@Test
	public void defaultConstructorCreatesDefaultNode() {
	    Freenet7Interface freenet7Interface = new Freenet7Interface();
		freenet7Interface.setNode(new Node("foo", 12345));
		de.todesbaum.util.freenet.fcp2.Node node = freenet7Interface.getNode();
		assertThat(node.getHostname(), is("foo"));
		assertThat(node.getPort(), is(12345));
	}

	@Test
	public void withoutSettingANodeThereIsNoNode() {
	    assertThat(freenet7Interface.hasNode(), is(false));
	}

	@Test
	public void afterSettingANodeThereIsANode() {
		when(nodeSupplier.supply(anyString(), anyInt())).thenReturn(mock(de.todesbaum.util.freenet.fcp2.Node.class));
		when(connectionSupplier.supply(any(de.todesbaum.util.freenet.fcp2.Node.class), anyString())).thenReturn(mock(Connection.class));
	    freenet7Interface.setNode(new Node("foo", 12345));
	    assertThat(freenet7Interface.hasNode(), is(true));
	}

	@Test
	public void withoutConnectionThereIsNoNode() {
		when(nodeSupplier.supply(anyString(), anyInt())).thenReturn(mock(de.todesbaum.util.freenet.fcp2.Node.class));
	    freenet7Interface.setNode(new Node("foo", 12345));
	    assertThat(freenet7Interface.hasNode(), is(false));
	}

	@Test
	public void defaultConstructorCreatesDefaultConnection() {
		Freenet7Interface freenet7Interface = new Freenet7Interface();
		Connection connection = freenet7Interface.getConnection("foo");
		assertThat(connection.getName(), is("foo"));
	}

	@Test
	public void settingNodeAddressUsesNodeAndConnectionSuppliers() {
		Node node = new Node(NODE_ADDRESS, NODE_PORT);
		freenet7Interface.setNode(node);
		verify(nodeSupplier).supply(NODE_ADDRESS, NODE_PORT);
		verify(connectionSupplier).supply(eq(node), anyString());
	}

	@Test
	public void settingNodeRetainsNodeCorrectly() {
		Node node = new Node(NODE_ADDRESS, NODE_PORT);
		Node realNode = mock(Node.class);
		when(nodeSupplier.supply(NODE_ADDRESS, NODE_PORT)).thenReturn(realNode);
		freenet7Interface.setNode(node);
		assertThat(freenet7Interface.getNode(), is(realNode));
	}

	@Test
	public void newConnectionCanBeCreated() {
		Connection connection = mock(Connection.class);
		when(connectionSupplier.supply(any(Node.class), eq(IDENTIFIER))).thenReturn(connection);
		Connection returnedConnection = freenet7Interface.getConnection(IDENTIFIER);
		assertThat(returnedConnection, is(connection));
	}

	@Test
	public void interfaceHasNoNodeBeforeNodeIsSet() {
		assertThat(freenet7Interface.hasNode(), is(false));
	}

	@Test
	public void interfaceHasNodeOnceANodeWasSet() {
		Connection connection = mock(Connection.class);
		when(nodeSupplier.supply(anyString(), anyInt())).thenReturn(mock(Node.class));
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		assertThat(freenet7Interface.hasNode(), is(true));
	}

	@Test
	public void interfaceHasNoNodeIfNodeIsSetToNull() {
		Connection connection = mock(Connection.class);
		when(nodeSupplier.supply(anyString(), anyInt())).thenReturn(mock(Node.class));
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		freenet7Interface.setNode(null);
		assertThat(freenet7Interface.hasNode(), is(false));
	}


	@Test
	public void nodeIsPresentIfConnectionIsConnected() throws IOException {
		Connection connection = mock(Connection.class);
		when(connection.isConnected()).thenReturn(true);
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		assertThat(freenet7Interface.isNodePresent(), is(true));
	}

	@Test
	public void nodeIsPresentIfConnectionCanBeCreated() throws IOException {
		Connection connection = mock(Connection.class);
		when(connection.connect()).thenReturn(true);
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		assertThat(freenet7Interface.isNodePresent(), is(true));
	}

	@Test
	public void exceptionIsThrownIfNodeIsNotPresentAndConnectionCanNotBeCreated() throws IOException {
		Connection connection = mock(Connection.class);
		doThrow(IOException.class).when(connection).connect();
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		expectedException.expect(IOException.class);
		freenet7Interface.isNodePresent();
	}

	@Test
	public void keyPairIsNotGeneratedIfNodeIsNotPresent() throws IOException {
		Connection connection = mock(Connection.class);
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		expectedException.expect(IOException.class);
		freenet7Interface.generateKeyPair();
	}

	@Test
	public void keyPairIsGeneratedSuccessfully() throws IOException {
		Connection connection = mock(Connection.class);
		when(connection.isConnected()).thenReturn(true);
		when(connectionSupplier.supply(any(Node.class), anyString())).thenReturn(connection);
		freenet7Interface.setNode(mock(Node.class));
		Message message = new Message("SSKKeyPair");
		message.put("InsertURI", INSERT_URI);
		message.put("RequestURI", REQUEST_URI);
		Client client = mock(Client.class);
		when(client.readMessage()).thenReturn(message);
		when(clientSupplier.supply(eq(connection), Mockito.any(GenerateSSK.class))).thenReturn(client);
		String[] keyPair = freenet7Interface.generateKeyPair();
		assertThat(keyPair[0], is(INSERT_URI));
		assertThat(keyPair[1], is(REQUEST_URI));
	}

}
