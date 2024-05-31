package ibm.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Planet", description = "Planet representation")
public record Planet(@Schema(required = true) String name, @Schema(required = true) Double distance) {

}
