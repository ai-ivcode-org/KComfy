KComfy
======

Kotlin client for interacting with ComfyUI.

Project summary
---------------
KComfy is a small Kotlin library + example workflows that demonstrate how to compose and send prompt requests to a ComfyUI-style backend. It includes templating (Mustache), Retrofit-based HTTP client wiring, and example workflows such as checkpoint-based text-to-image.

Usage
------

```kotlin

// Create the ComfyUI API client.
val kComfy = KComfy.create("http://localhost:8000")

kComfy.use { api ->
    // Send a prompt request to generate an image using a specific checkpoint.
    api.prompt(CheckpointTextToImage (
        checkpoint = "sdXL_v10VAEFix",
        prompt = "a cartoon style illustration of a happy astronaut riding a horse in space, vibrant colors, detailed, high quality",
        negativePrompt = "lowres, bad anatomy, error body, error hands, error fingers"
    )).use {
        // Wait for the job to complete. Get the outputs when done.
        val outputs = it.getOutputs().get()

        // Save each output image to a file.
        outputs.forEach { output ->
            val input = api.view(output)
            File(output.filename).outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    } // on prompt-job close, remove jobs from server
} // on closing KComfy, clean up resources and close web-socket

```

Workflows
---------
ComfyUI workflows are defined using Mustache templates + Kotlin model objects. The library includes example workflows
under the `workflows` package.

For this example, see these files within the project:
```
src/
└── main/
    ├── kotlin/
    │   └── workflows/
    │       └── CheckpointTextToImage.kt
    └── resources/
        └── comfy/
            └── checkpoint_text_to_image.json.mustache
```

Key files:
- [`CheckpointTextToImage`](src/main/kotlin/workflows/CheckpointTextToImage.kt)
- [`checkpoint_text_to_image.json.mustache`](src/main/resources/comfy/checkpoint_text_to_image.json.mustache)


In this example we load in a standard ComfyUI checkpoint-based text-to-image workflow. The important part of this
example is this `@WorkflowTemplate` annotation. This tells KComfy where to find the Mustache template for this workflow.

```kotlin
@WorkflowTemplate("comfy/checkpoint_text_to_image.json.mustache")
data class CheckpointTextToImage(
    var checkpoint: String,
    var prompt: String,
    var negativePrompt: String = "",
    var width: Int = 1024,
    var height: Int = 1024,
    var batch: Int = 1,
    var steps: Int = 30,
    var cfg: Double = 7.0,
    var sampler: SamplerName = SamplerName.EULER,
    var scheduler: Scheduler = Scheduler.NORMAL,
    var seed: Long = Random.nextLong(0, Long.MAX_VALUE),
    var denoise: Double = 1.0
)
```

The template is a parameterized ComfyUI workflow export. These are represented as mustache templates. These templates
are assumed to be in the classpath. If you're using Gradle, you can place them in `src/main/resources`. Otherwise,
place them in your source folder and ensure they are included in the classpath at runtime.

The parameter names will be mapped to the properties of the data class. The value `{{prompt}}`, for example, will be replaced
with the value of the `prompt` property when the workflow is rendered.

For information on exporting ComfyUI workflows and how templates map to the exported JSON, see this guide:
- https://medium.com/@next.trail.tech/how-to-use-comfyui-api-with-python-a-complete-guide-f786da157d37


Job Management
----------------
ComfyUI wasn't designed as a REST API-first system, so job management is happening behind the scenes. It's mostly
abstracted away, but you should be aware of what's happening.

A job in ComfyUI goes through the following lifecycle:

1. **Create Job**: When you send a prompt request, a new job is created
2. **Pending**: The job is queued for processing
3. **Processing**: The job is actively being processed by ComfyUI
4. **History**: Once complete, the job is moved to history for later retrieval

When a job is closed using the `Closeable` interface, KComfy will attempt to delete the job from the ComfyUI server to
free up resources. If closed while still processing, the job may be cancelled, or it may get lost.

KComfy uses a WebSocket connection to listen for job status updates. This connection is managed automatically. When you
close the `KComfy` client, the WebSocket connection is closed as well. Jobs will no longer be updated, though 
non-websocket communications (like pulling outputs) will still work.