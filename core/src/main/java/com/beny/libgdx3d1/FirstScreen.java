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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.math.collision.BoundingBox;

import com.beny.libgdx3d1.GameClient;

import com.badlogic.gdx.Screen;

import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.Intersector;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Array;

import java.util.UUID;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import com.beny.libgdx3d1.PojoNetworkUpdate;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

	private Vector3 pos = new Vector3(); // <--- declare here
	private AssetManager assetManager = new AssetManager();
	private Array<ModelInstance> instances = new Array<>();
	private Array<AnimationController> controllers = new Array<>();

	private Stage stage;              // for UI overlay, optional
	private float rotateSpeed = 90f;  // degrees per second
	private float moveSpeed = 0.009f;     // units per second

	//private NetworkClient networkClient;

	// 3D Rendering
	private PerspectiveCamera camera = new PerspectiveCamera();
	private Environment environment;
	private ModelBatch modelBatch;

	// Models
	private Model medeaBlendModel, terrainModel, poleModel, medeaBlendModel2;
	private ModelInstance medeaBlendInstance, terrainInstance, poleInstance;

	// Models Instances
	private ModelInstance currentInstance, modelInstance;

	// Animation Controllers
	private AnimationController medeaBlendAnimController;
	private AnimationController currentAnimation;
	private boolean isWalking = false;
	//private UUID uuid = UUID.randomUUID();
	private Gson gson = new Gson();
	private Map<String, Object> message = new HashMap<>();

	// Create an array to contain other models in the network
	private ArrayList<Object[]> modelAnims = new ArrayList<>();
	private PojoNetworkUpdate pojoNetworkUpdate = new PojoNetworkUpdate();

	//private final String localClientId = UUID.randomUUID().toString();
	private final UUID localClientId = UUID.randomUUID();

	private final Vector3 tmpPos = new Vector3();
	private final Quaternion tmpRot = new Quaternion();
	// Decide once how big your model should be
	private final Vector3 baseScale = new Vector3(0.4f, 0.4f, 0.4f); 


	/*
	 * it is used for UDP and TCP Connection
	try {
		networkClient = new NetworkClient();
		networkClient.connect("localhost"); // Or server IP
	} catch (Exception e) {
		e.printStackTrace();
	}
	 */

	public GameClient client = new GameClient("ws://localhost:8080");

	@Override
	public void show() {
		Gdx.input.setInputProcessor(null);
		client.connect();

		// Prepare your screen here.
		// camera
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

		ModelBuilder modelBuilder = new ModelBuilder();
		poleModel = modelBuilder.createBox(
				0.2f, 5f, 0.2f,  // width, height, depth
				new Material(ColorAttribute.createDiffuse(Color.RED)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
				);

		poleInstance = new ModelInstance(poleModel);
		poleInstance.transform.translate(-0.5f, 2.5f, 0f); // raise pole so base touches ground


		// Load medea model using AssetManager
		assetManager.load("models/MedeaBlend.g3db", Model.class);
		assetManager.finishLoadingAsset("models/MedeaBlend.g3db");
		medeaBlendModel = assetManager.get("models/MedeaBlend.g3db", Model.class);
		medeaBlendInstance = new ModelInstance(medeaBlendModel);
		medeaBlendInstance.transform.translate(0f, 0f, 0f);
		medeaBlendInstance.transform.setToScaling(0.4f, 0.4f, 0.4f); // scale down to 2%
		// controllers
		medeaBlendAnimController = new AnimationController(medeaBlendInstance);
		// start with "idle" as default
		medeaBlendAnimController.setAnimation("Armature|Idle");
		stateMessage(medeaBlendInstance,"Armature|Idle");


		// Load terrain model using AssetManager
		assetManager.load("models/terrain.g3db", Model.class);
		assetManager.finishLoadingAsset("models/terrain.g3db");
		terrainModel = assetManager.get("models/terrain.g3db", Model.class);
		terrainInstance = new ModelInstance(terrainModel);
		terrainInstance.transform.translate(0f, 0f, 0f);
		terrainInstance.transform.setToScaling(0.4f, 0.4f, 0.4f);


		/*
		for (Node node : terrainModel.nodes) {
		    System.out.println("Node: " + node.id + " parts: " + node.parts.size);
		}
		 */
		//System.out.println("Terrain meshes: " + terrainModel.meshes.size);

		// instances

		fitscreen(terrainInstance);

		//instances.add(poleInstance);
		//instances.add(medeaBlendInstance);

		//instances.add(terrainInstance);

		//setCurrentAnimation(medeaBlendModel, medeaBlendInstance, medeaBlendAnimController,"Armature|Idle");

	}

	private void setCurrentAnimation(Model model, ModelInstance instance, AnimationController controller, String animId) {
		/* preserve current transform
		if (currentInstance != null) {
			instance.transform.set(currentInstance.transform);
		}
		 */
		//currentInstance = instance;
		//currentAnimation = controller;


		//if (model != null && model.animations != null && model.animations.size > 0) {

		String animationId = "default".equals(animId) ? model.animations.get(0).id : animId;
		controller.setAnimation(animationId, -1); 
		//}
	}

	@Override
	public void render(float delta) {
		// Draw your screen here. "delta" is the time since last render in seconds.
		handleInput(delta);
		handleMessage(delta);

		if (medeaBlendAnimController != null) medeaBlendAnimController.update(delta);

		for(Object[] modelAnim : modelAnims) {
			//System.out.println("Nge update: " + String.valueOf(modelAnim[2]) + " - " + System.currentTimeMillis());
			if (modelAnim[2] != null) { 
				AnimationController animationControllerO = (AnimationController) modelAnim[2];
				animationControllerO.update(delta);
			}
		}


		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);
		//if (currentInstance != null) modelBatch.render(currentInstance, environment);
		modelBatch.render(terrainInstance, environment);
		modelBatch.render(medeaBlendInstance, environment);
		modelBatch.render(poleInstance, environment);
		/*
		for (Node node : terrainModel.nodes) {
		    System.out.println("Node: " + node.id + " parts: " + node.parts.size);
		}
		 */
		for(Object[] modelAnim : modelAnims) {
			//System.out.println("Nge render: " + String.valueOf(modelAnim[0]) + " - " + System.currentTimeMillis());
			if((ModelInstance) modelAnim[1] != null) modelBatch.render((ModelInstance) modelAnim[1], environment);
		}

		//if(medeaBlendInstance != null) System.out.println("Nge render medeaBlendInstance: " + String.valueOf(medeaBlendInstance) + " - " + System.currentTimeMillis());

		modelBatch.end();

		if (stage != null) {
			stage.act(delta);
			stage.draw();
		}
	}

	private void handleInput(float delta) {
		//if (currentInstance == null) return;
		float speed = moveSpeed * delta; // delta = Gdx.graphics.getDeltaTime()
		Vector3 forward = new Vector3(0f, 0f, 1f);
		Vector3 modelPos = new Vector3();
		pos.add(forward.scl(speed));

		// --- Camera Follow (Fixed Offset) ---
		medeaBlendInstance.transform.getTranslation(modelPos);

		// Keep camera at a fixed offset relative to the model
		camera.position.set(modelPos.x, modelPos.y+30f, modelPos.z+10f);

		// Always look at the model
		camera.lookAt(modelPos.x, modelPos.y, modelPos.z);
		//camera.up.set(Vector3.Y); // prevent upside-down tilt
		camera.update();
		//}

		// rotation (yaw) - using Matrix4.rotate(float x, float y, float z, float degrees)
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			medeaBlendInstance.transform.rotate(0f, 1f, 0f, rotateSpeed * delta);
			stateMessage(medeaBlendInstance,"Armature|walking");
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			medeaBlendInstance.transform.rotate(0f, 1f, 0f, -rotateSpeed * delta);
			stateMessage(medeaBlendInstance,"Armature|walking");
		}
		if (!pos.isZero()) {
			//currentInstance.transform.translate(pos);
		}

		// movement
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			// Start walking if not already walking
			if (!isWalking) {
				medeaBlendAnimController.setAnimation("Armature|walking", -1); // -1 = loop forever
				stateMessage(medeaBlendInstance,"Armature|walking");
				isWalking = true;
			}

			medeaBlendInstance.transform.translate(pos);
			stateMessage(medeaBlendInstance,"Armature|walking");
		}
		else {
			// Switch back to idle only once
			if (isWalking) {
				medeaBlendAnimController.setAnimation("Armature|Idle", -1);
				stateMessage(medeaBlendInstance,"Armature|Idle");
				isWalking = false;
			}
		}


	}

	private void handleMessage(float delta) {
		//if (currentInstance == null) return;
		String raw = client.getLatestMessage();
		if (raw == null || raw.isEmpty()) return;
		// skip the welcome string
		if (!raw.trim().startsWith("{")) {
			System.out.println("Skipping non-JSON: " + raw);
			return;
		}

		if(raw != null) {
			try {
				pojoNetworkUpdate = gson.fromJson(raw, PojoNetworkUpdate.class);
			} catch (Exception e) {
				return; // not JSON or wrong shape – ignore ---
			}
			if (pojoNetworkUpdate != null) {
				//if(findModelAnimByUuid(UUID.fromString(pojoNetworkUpdate.getUuid())) == null ) {
				UUID netUuid = pojoNetworkUpdate.getUuid();
				if(findModelAnimByUuid(netUuid) == null) {
					if(!netUuid.equals(localClientId)) { 
						System.out.println("Kirim stateMessage di awal");
						stateMessage(medeaBlendInstance,"Armature|Idle");
						insertModelAnims(netUuid);
					}
				}
				else {
					ModelInstance modelInstance1 = (ModelInstance) findModelAnimByUuid(netUuid)[1];
					AnimationController animationController1 = (AnimationController) findModelAnimByUuid(netUuid)[2];
					applyState(modelInstance1,animationController1,pojoNetworkUpdate.getPos(),pojoNetworkUpdate.getHeading(),pojoNetworkUpdate.getAni());
				}
			}

		}
	}

	public Object[] findModelAnimByUuid(UUID oUuid) {
		if(modelAnims != null) {
			for(Object[] modelAnim : modelAnims) {
				if(oUuid.equals(modelAnim[0])) {
					return modelAnim;
				}
			}
		}
		return null;
	}

	public void insertModelAnims(UUID otherUuid) {
		ModelInstance medeaBlendInstanceNet = new ModelInstance(medeaBlendModel);
		//medeaBlendInstanceNet.transform.translate(0f, 0f, 0f);
		medeaBlendInstanceNet.transform.setToScaling(0.4f, 0.4f, 0.4f); // scale down to 2%
		// controllers
		AnimationController medeaBlendAnimControllerNet = new AnimationController(medeaBlendInstanceNet);
		// start with "idle" as default
		medeaBlendAnimControllerNet.setAnimation("Armature|Idle");
		Object[] modelAnim = {otherUuid, medeaBlendInstanceNet, medeaBlendAnimControllerNet};
		modelAnims.add(modelAnim);
		System.out.println("Inserted: " + String.valueOf(modelAnim[0]));

	}

	private void stateMessage(ModelInstance modelInstance, String animation) {
		try {
			message = new HashMap<>();
			message.put("uuid", localClientId);
			message.put("actType", "state");

			// include the current position info
			Vector3 posSt = new Vector3();
			modelInstance.transform.getTranslation(posSt);
			message.put("pos", posSt);

			// include the current heading info
			Quaternion rotation2 = new Quaternion();
			//Matrix4 rotation2 = new Matrix4();
			modelInstance.transform.getRotation(rotation2);
			Vector3 forward2 = new Vector3(0f, 0f, 1f);
			rotation2.transform(forward2);
			float heading = (float)Math.toDegrees(Math.atan2(forward2.x, forward2.z));
			message.put("heading", heading);

			// include the current animation info
			message.put("ani", animation);

			if (client.isOpen()) {
				client.send(gson.toJson(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		assetManager.dispose();

		if (medeaBlendModel != null) medeaBlendModel.dispose();
		if (terrainModel != null) terrainModel.dispose();
		if (poleModel != null) poleModel.dispose();
	}

	/*
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	 */

	private Vector3 parseVector3(String str) {
		String[] parts = str.split(",");
		if (parts.length != 3) throw new IllegalArgumentException("Invalid Vector3 string: " + str);
		float x = Float.parseFloat(parts[0]);
		float y = Float.parseFloat(parts[1]);
		float z = Float.parseFloat(parts[2]);
		return new Vector3(x, y, z);
	}

	private void applyState(ModelInstance rp, AnimationController ac, Vector3 inpos,  float msgyaw, String msganim) {

		// 1) build absolute translation
		tmpPos.set(inpos.x, inpos.y, inpos.z);

		// 2) build absolute rotation: yaw around Y
		tmpRot.setEulerAngles(msgyaw, 0f, 0f); // LibGDX: yaw(Y), pitch(X), roll(Z)

		// 3) compose TRS in one shot (NO cumulative ops)
		rp.transform.set(tmpPos, tmpRot, baseScale);

		// 4) animation (idempotent)
		if (msganim != null) {
			if (ac.current == null || !ac.current.animation.id.equals(msganim)) {
				ac.setAnimation(msganim, -1);
			}
		}
	}
}

