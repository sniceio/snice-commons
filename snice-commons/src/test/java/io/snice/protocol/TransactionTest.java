package io.snice.protocol;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.ensureNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TransactionTest {

    private Request<String, Object> request;
    private TransactionId transactionId;

    @Before
    public void setUp() {
        transactionId = mock(TransactionId.class);
        request = RequestSupport.of("bill").withTransactionId(transactionId).build();
    }

    /**
     * More of a look-and-feel test really. API is important so this test is really just
     * taking the API for a ride...
     */
    @Test
    public void testSupportingClassesNoPayload() {
        final Request<String, Object> noPayload = RequestSupport.of("nisse").build();
        assertRequest(noPayload, "nisse");
    }

    @Test
    public void testSupportingClassesWithPayload() {
        final Request<Integer, String> request = RequestSupport.of(45, "my payload").build();
        assertThat(request.getOwner(), is(45));
        assertThat(request.getPayload(), is(Optional.of("my payload")));
        assertThat(request.getTransactionId(), notNullValue());
    }

    @Test
    public void testCreator() {
        assertRequest(RequestSupport.create("alice"), "alice");
    }

    @Test
    public void testSpecifTransactionId() {
        assertThat(request.getOwner(), is("bill"));
        assertThat(request.getPayload(), is(Optional.empty()));
        assertThat(request.getTransactionId(), is(transactionId));
    }

    private static void assertRequest(final Request<String, Object> request, final String owner) {
        assertThat(request.getOwner(), is(owner));
        assertThat(request.getPayload(), is(Optional.empty()));
        assertThat(request.getTransactionId(), notNullValue());
    }

    @Test
    public void testCreateResponse() {
        final Response<String, Integer> response = request.buildResponse(123).build();

        // isFinal is by default true
        assertThat(response.isFinal(), is(true));

        // should have same owner as the request
        assertThat(response.getOwner(), is("bill"));

        // the transaction id should always be the same as the request.
        assertThat(response.getTransactionId(), is(transactionId));
        assertThat(response.getPayload(), is(Optional.of(123)));
    }

    @Test
    public void testCreateProvisionalResponse() {
        final Response<String, Object> response = request.buildResponse().isFinal(false).build();
        assertThat(response.isFinal(), is(false));
        assertThat(response.getPayload(), is(Optional.empty()));
    }

    /**
     * The purpose of the {@link Transaction}, and the {@link Request}, {@link Response} interfaces is to
     * make it easy to of subclasses of them so that they can be used in various situations, such as
     * in hektor.io for passing messages between actors and also for the hektor.io FSM.
     */
    @Test
    public void testCreateSubClassesNoPayload() {
        final MyRequestNoPayload request = new MyRequestNoPayload("nisse");
        assertThat(request.getOwner(), is("nisse"));
        assertThat(request.getPayload(), is(Optional.empty()));
        assertThat(request.getTransactionId(), notNullValue());

        final MyResponseNoPayload response = request.createResponse();
        assertThat(response.getTransactionId(), is(request.getTransactionId()));
        assertThat(response.getOwner(), is(request.getOwner()));
    }

    @Test
    public void testCreateSubClassesPayload() {

    }

    public static class MyRequestNoPayload extends RequestSupport<String, Object> {
        protected MyRequestNoPayload(final String owner) {
            super(ensureNotNull(owner));
        }

        @Override
        public MyResponseNoPayload createResponse() {
            return new MyResponseNoPayload(getTransactionId(), getOwner());
        }
    }

    public static class MyResponseNoPayload extends ResponseSupport<String, Object> {

        public MyResponseNoPayload(final TransactionId transactionId, final String owner) {
            super(transactionId, owner);
        }
    }

    public static class MyRequest<T> extends RequestSupport<String, T> {

        public static MyRequest<Object> create(final String owner) {
            return new MyRequest(owner);
        }

        protected MyRequest(final String owner) {
            super(owner);
        }

        protected Request<String, T> internalBuild(final TransactionId id, final String owner, final Optional<T> payload) {
            return new RequestSupport<>(id, owner, payload);
        }

    }

}