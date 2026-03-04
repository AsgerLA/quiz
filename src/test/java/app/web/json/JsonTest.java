package app.web.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTest
{
    @Test
    void good_object()
            throws JsonException
    {
        String JSON = """
    {
        "string" : "string",
        "int" : 1,
        "double" : -0.5,
        "boolean" : true,
        "null" : null
    }
    """;
        JsonParser.decodeObject(JSON);
    }

    @Test
    void good_array()
            throws JsonException
    {
        String JSON = """
    [
        {
            "string" : "string",
            "int" : 1,
            "double" : -0.5,
            "boolean" : true,
            "null" : null
        },
        {
            "string" : "string",
            "int" : 1,
            "double" : -0.5,
            "boolean" : true,
            "null" : null
        }
    ]
    """;
        JsonParser.decodeArray(JSON);
    }

    @Test
    void good_complex()
            throws JsonException
    {
        String JSON = """
    {
        "string" : "string",
        "int" : 1,
        "double" : -0.5,
        "boolean" : true,
        "null" : null,
        "inner-object" : {
            "id" : 1,
            "array" : [
                {
                    "string" : "string",
                    "int" : 1,
                    "double" : -0.5,
                    "boolean" : true,
                    "null" : null
                },
                {
                    "string" : "string",
                    "int" : 1,
                    "double" : -0.5,
                    "boolean" : true,
                    "null" : null
                }
            ]
        }
    }
    """;
        JsonParser.decodeObject(JSON);
    }

    @Test
    void bad_object()
    {
        String JSON = """
    {
        "string" : "string",
        "int" : 1
        "double" : -0.5,
        "boolean" : true,
        "null" : null
    }
    """;
        assertThrows(JsonException.class, () -> {
            JsonParser.decodeObject(JSON);
        });
    }

    @Test
    void bad_array()
    {
        String JSON = """
    [
        {
            "string" : "string",
            "int" : 1,
            "double" : -0.5,
            "boolean" : true,
            "null" : null
        }
    }
    """;
        assertThrows(JsonException.class, () -> {
            JsonParser.decodeArray(JSON);
        });
    }

    @Test
    void bad_complex()
    {
        // too deeply nested
        String JSON = """
    {
        "string" : "string",
        "int" : 1,
        "double" : -0.5,
        "boolean" : true,
        "null" : null
        "inner-object" : {
            "id" : 1,
            "array" : [
                {
                    "string" : "string",
                    "int" : 1,
                    "double" : -0.5,
                    "boolean" : true,
                    "null" : null,
                    "inner-object" : {
                        "id" : 1,
                        "array" : [
                            {
                                "string" : "string",
                                "int" : 1,
                                "double" : -0.5,
                                "boolean" : true,
                                "null" : null,
                                "int-array" : [1,2,3,4]
                            },
                            {
                                "string" : "string",
                                "int" : 1,
                                "double" : -0.5,
                                "boolean" : true,
                                "null" : null
                            }
                        ]
                    }
                }
            ]
        }
    }
    """;
        assertThrows(JsonException.class, () -> {
            JsonParser.decodeObject(JSON);
        });
    }

    @Test
    void bad_unclosedString()
    {
        String JSON = """
    {
        "name" : "unclosed,
    }
    """;
        assertThrows(JsonException.class, () -> {
            JsonParser.decodeObject(JSON);
        });
    }
}
