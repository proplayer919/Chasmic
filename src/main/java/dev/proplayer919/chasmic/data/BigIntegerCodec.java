package dev.proplayer919.chasmic.data;

import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.math.BigInteger;

/**
 * Mongo codec for BigInteger values used in player profile progression data.
 */
public class BigIntegerCodec implements Codec<BigInteger> {
    @Override
    public BigInteger decode(BsonReader reader, DecoderContext decoderContext) {
        BsonType type = reader.getCurrentBsonType();

        return switch (type) {
            case NULL -> {
                reader.readNull();
                yield BigInteger.ZERO;
            }
            case INT32 -> BigInteger.valueOf(reader.readInt32());
            case INT64 -> BigInteger.valueOf(reader.readInt64());
            case DECIMAL128 -> reader.readDecimal128().bigDecimalValue().toBigInteger();
            case STRING -> {
                String value = reader.readString();
                if (value == null || value.isBlank()) {
                    yield BigInteger.ZERO;
                }
                try {
                    yield new BigInteger(value);
                } catch (NumberFormatException exception) {
                    throw new CodecConfigurationException("Invalid BigInteger string value: " + value, exception);
                }
            }
            default -> throw new CodecConfigurationException("Unsupported BSON type for BigInteger: " + type);
        };
    }

    @Override
    public void encode(BsonWriter writer, BigInteger value, EncoderContext encoderContext) {
        if (value == null) {
            writer.writeNull();
            return;
        }

        // Store as string to preserve arbitrary precision.
        writer.writeString(value.toString());
    }

    @Override
    public Class<BigInteger> getEncoderClass() {
        return BigInteger.class;
    }
}

