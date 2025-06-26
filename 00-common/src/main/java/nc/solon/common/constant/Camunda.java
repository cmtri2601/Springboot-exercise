package nc.solon.common.constant;

public class Camunda {
    public static class Types {
        public final static String CREATE_PERSON = "create-person";
        public final static String CREATE_PERSON_FAIL = "create-person-fail";
    }

    public static class Variables {
        public final static String PERSON_ID = "id";
        public final static String ERROR_MESSAGE = "errorMessage";
        public final static String STATUS = "status";
    }

    public static class Messages {
        public final static String APPROVE = "approve";
        public final static String REJECT = "reject";
    }

    public static class Errors {
        public final static String CREATE_PERSON_FAIL = "CREATE_PERSON_FAIL";
        public final static String ADD_TAX_FAIL = "ADD_TAX_FAIL";
    }

    public static class KafkaLogMessages {
        public final static String CREATE_PERSON_FAIL = "Fail to create person by job worker";
        public final static String ADD_TAX_FAIL = "Fail to add tax by by rest outbound";
    }
}
