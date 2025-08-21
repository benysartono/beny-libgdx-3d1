package com.beny.libgdx3d1;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.math.collision.BoundingBox;



import com.badlogic.gdx.Screen;



/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {


	private Stage stage;              // for UI overlay, optional
	private float rotateSpeed = 90f;  // degrees per second
	private float moveSpeed = 5f;     // units per second

	private NetworkClient networkClient;

	// 3D Rendering
	private PerspectiveCamera camera;
	private Environment environment;
	private ModelBatch modelBatch;

	// Models
	private Model runModel, walkModel, shootModel, combineModel;
	private ModelInstance runInstance, walkInstance, shootInstance, combineInstance;

	private ModelInstance currentInstance;
	private AnimationController currentAnimation;

	// Animation Controllers
	private AnimationController runAnimController, walkAnimController, shootAnimController, combineAnimController;

	/*
	 * it is used for UDP and TCP Connection
	try {
		networkClient = new NetworkClient();
		networkClient.connect("localhost"); // Or server IP
	} catch (Exception e) {
		e.printStackTrace();
	}
	*/
	
	GameClient client = new GameClient("ws://localhost:8080");
	client.connect();

	@Override
	public void show() {
		// Prepare your screen here.
		// camera
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//camera.position.set(3f, 8f, 18f);
		//camera.lookAt(0f, 1f, 0f);
		camera.near = 0.1f;
		camera.far = 300f;
		camera.update();

		// environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f,0.8f,0.8f,1f));
		environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		// load models from core/assets/models/
		UBJsonReader reader = new UBJsonReader();
		G3dModelLoader loader = new G3dModelLoader(reader);

		runModel = loader.loadModel(Gdx.files.internal("models/Run.g3db"));
		walkModel = loader.loadModel(Gdx.files.internal("models/Walking.g3db"));
		shootModel = loader.loadModel(Gdx.files.internal("models/Shooting.g3db"));
		combineModel = loader.loadModel(Gdx.files.internal("models/blend1.g3db"));

		// instances
		runInstance = new ModelInstance(runModel);
		walkInstance = new ModelInstance(walkModel);
		shootInstance = new ModelInstance(shootModel);
		combineInstance = new ModelInstance(combineModel);

		for (com.badlogic.gdx.graphics.g3d.model.Animation anim : combineModel.animations) {
			System.out.println("Kode Animasi: " + anim.id);
		}

		// position them at origin initially (optional)
		runInstance.transform.setToTranslation(0f, 0f, 0f);
		walkInstance.transform.setToTranslation(0f, 0f, 0f);
		shootInstance.transform.setToTranslation(0f, 0f, 0f);
		combineInstance.transform.setToTranslation(0f, 0f, 0f);

		fitscreen(runInstance);
		runInstance.transform.setToScaling(0.02f, 0.02f, 0.02f); // scale down to 2%
		fitscreen(walkInstance);
		walkInstance.transform.setToScaling(0.02f, 0.02f, 0.02f); // scale down to 2%
		fitscreen(shootInstance);
		shootInstance.transform.setToScaling(0.02f, 0.02f, 0.02f); // scale down to 2%
		fitscreen(combineInstance);
		combineInstance.transform.setToScaling(0.02f, 0.02f, 0.02f); // scale down to 2%


		// controllers
		runAnimController = new AnimationController(runInstance);
		walkAnimController = new AnimationController(walkInstance);
		shootAnimController = new AnimationController(shootInstance);
		combineAnimController = new AnimationController(combineInstance);

		// start with "walk" as default
		combineAnimController.setAnimation("Death", 2);
		setCurrentAnimation(combineModel, combineInstance, combineAnimController,"default");
	}

	private void setCurrentAnimation(Model model, ModelInstance instance, AnimationController controller, String animId) {
		// preserve current transform
		if (currentInstance != null) {
			instance.transform.set(currentInstance.transform);
		}

		currentInstance = instance;
		currentAnimation = controller;

		if (model != null && model.animations != null && model.animations.size > 0) {
			String animationId = "default".equals(animId) ? model.animations.get(0).id : animId;
			currentAnimation.setAnimation(animationId, 2); // loop
		}
	}

	@Override
	public void render(float delta) {
		// Draw your screen here. "delta" is the time since last render in seconds.
		handleInput(delta);

		if (currentAnimation != null) currentAnimation.update(delta);

		// camera follow: place camera relative to current instance
		if (currentInstance != null) {
			Vector3 pos = new Vector3();
			currentInstance.transform.getTranslation(pos);
			// third-person offset (tweak as needed)
			camera.position.set(pos.x, pos.y + 3f, pos.z + 8f);
			camera.lookAt(pos.x, pos.y + 1.5f, pos.z);
			camera.update();
		}

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);
		if (currentInstance != null) modelBatch.render(currentInstance, environment);
		modelBatch.end();

		if (stage != null) {
			stage.act(delta);
			stage.draw();
		}
	}


	private void handleInput(float delta) {
		if (currentInstance == null) return;


		// animation switching
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			setCurrentAnimation(runModel, runInstance, runAnimController, "default");
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			setCurrentAnimation(walkModel, walkInstance, walkAnimController, "default");
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			setCurrentAnimation(shootModel, shootInstance, shootAnimController, "default");
		}

		// rotation (yaw) - using Matrix4.rotate(float x, float y, float z, float degrees)
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			currentInstance.transform.rotate(0f, 1f, 0f, rotateSpeed * delta);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			currentInstance.transform.rotate(0f, 1f, 0f, -rotateSpeed * delta);
		}

		// forward vector = (0,0,-1) rotated by instance rotation quaternion
		Quaternion rot = new Quaternion();
		currentInstance.transform.getRotation(rot);
		Vector3 forward = new Vector3(0f, 0f, -1f);
		rot.transform(forward);   // <-- this rotates the vector by the Quaternion
		forward.nor();

		// movement
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			Vector3 movement = new Vector3(forward).scl(moveSpeed * delta);
			currentInstance.transform.translate(movement.x, movement.y, movement.z);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			Vector3 movement = new Vector3(forward).scl(-moveSpeed * delta);
			currentInstance.transform.translate(movement.x, movement.y, movement.z);
		}

	}

	public void fitscreen(ModelInstance instance){
		// Calculate model bounds
		BoundingBox bounds = new BoundingBox();
		instance.calculateBoundingBox(bounds);

		// Get center and dimensions
		Vector3 center = new Vector3();
		bounds.getCenter(center);
		Vector3 dimensions = new Vector3();
		bounds.getDimensions(dimensions);

		// Compute radius for fitting
		float radius = dimensions.len() / 2f;

		// Move camera so that model fits the screen
		float camDistance = radius * 2.5f; // adjust multiplier for zoom level
		camera.position.set(center.x, center.y + radius * 0.5f, center.z + camDistance);
		camera.lookAt(center);
		camera.update();

		// Translate model so its base is at y=0
		instance.transform.translate(-center.x, -bounds.min.y, -center.z);
	}


	@Override
	public void resize(int width, int height) {
		// If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
		// In that case, we don't resize anything, and wait for the window to be a normal size before updating.
		if(width <= 0 || height <= 0) return;

		// Resize your screen here. The parameters represent the new window size.
	}

	@Override
	public void pause() {
		// Invoked when your application is paused.
	}

	@Override
	public void resume() {
		// Invoked when your application is resumed after pause.
	}

	@Override
	public void hide() {
		// This method is called when another screen replaces this one.
	}

	@Override
	public void dispose() {
		// Destroy screen's assets here.
		modelBatch.dispose();
		if (runModel != null) runModel.dispose();
		if (walkModel != null) walkModel.dispose();
		if (shootModel != null) shootModel.dispose();
		if (combineModel != null) combineModel.dispose();
	}
}