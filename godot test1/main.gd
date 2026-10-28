extends Node2D

# The game uses a fixed 480 x 800 canvas, scaled to the device screen.
const WIDTH := 480.0
const HEIGHT := 800.0
const PLAYER_RADIUS := 18.0
const PLAYER_Y := 680.0
const MOVE_SPEED := 370.0

var player_x := WIDTH / 2.0
var obstacles: Array[Dictionary] = []
var elapsed := 0.0
var spawn_timer := 0.6
var best := 0
var playing := false
var dragging := false
var touch_id := -1
var score_label: Label
var best_label: Label
var overlay: ColorRect
var title_label: Label
var message_label: Label
var start_button: Button


func _ready() -> void:
	var save := ConfigFile.new()
	if save.load("user://score.cfg") == OK:
		best = int(save.get_value("game", "best", 0))
	build_ui()
	queue_redraw()


func make_label(text: String, rect: Rect2, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.position = rect.position
	label.size = rect.size
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", size)
	label.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(label)
	return label


func build_ui() -> void:
	score_label = make_label("TIME  0", Rect2(20, 28, 440, 55), 32)
	best_label = make_label("BEST  %d" % best, Rect2(20, 85, 440, 30), 18)
	best_label.modulate = Color("8ea5c8")
	var hint := make_label("←  MOVE  →   /   DRAG TO MOVE", Rect2(10, 748, 460, 30), 16)
	hint.modulate = Color("8ea5c8")
	overlay = ColorRect.new()
	overlay.size = Vector2(WIDTH, HEIGHT)
	overlay.color = Color(0.02, 0.035, 0.08, 0.86)
	add_child(overlay)
	title_label = make_label("STAR DODGE", Rect2(20, 230, 440, 70), 42)
	message_label = make_label("Dodge the falling blocks.\nArrow keys or drag to move.", Rect2(20, 315, 440, 90), 22)
	start_button = Button.new()
	start_button.text = "START"
	start_button.position = Vector2(110, 445)
	start_button.size = Vector2(260, 70)
	start_button.add_theme_font_size_override("font_size", 26)
	start_button.pressed.connect(start_game)
	add_child(start_button)


func start_game() -> void:
	player_x = WIDTH / 2.0
	obstacles.clear()
	elapsed = 0.0
	spawn_timer = 0.7
	dragging = false
	touch_id = -1
	playing = true
	set_menu_visible(false)
	score_label.text = "TIME  0"
	queue_redraw()


func set_menu_visible(show_menu: bool) -> void:
	overlay.visible = show_menu
	title_label.visible = show_menu
	message_label.visible = show_menu
	start_button.visible = show_menu


func _input(event: InputEvent) -> void:
	if not playing:
		return
	if event is InputEventScreenTouch:
		if event.pressed and touch_id == -1:
			touch_id = event.index
			player_x = clampf(event.position.x, 24.0, WIDTH - 24.0)
		elif not event.pressed and event.index == touch_id:
			touch_id = -1
	elif event is InputEventScreenDrag and event.index == touch_id:
		player_x = clampf(event.position.x, 24.0, WIDTH - 24.0)
	elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		dragging = event.pressed
		if dragging:
			player_x = clampf(event.position.x, 24.0, WIDTH - 24.0)
	elif event is InputEventMouseMotion and dragging:
		player_x = clampf(event.position.x, 24.0, WIDTH - 24.0)


func _notification(what: int) -> void:
	if what == NOTIFICATION_APPLICATION_FOCUS_OUT:
		dragging = false
		touch_id = -1


func _physics_process(delta: float) -> void:
	if not playing:
		return
	elapsed += delta
	var direction := Input.get_axis("ui_left", "ui_right")
	player_x = clampf(player_x + direction * MOVE_SPEED * delta, 24.0, WIDTH - 24.0)
	spawn_timer -= delta
	if spawn_timer <= 0.0:
		var side := randf_range(28.0, 56.0)
		obstacles.append({"rect": Rect2(randf_range(12.0, WIDTH - side - 12.0), -side, side, side), "speed": randf_range(190.0, 260.0) + minf(elapsed * 5.0, 220.0)})
		spawn_timer = maxf(0.25, 0.8 - elapsed * 0.009)
	for i in range(obstacles.size() - 1, -1, -1):
		var rect: Rect2 = obstacles[i]["rect"]
		rect.position.y += float(obstacles[i]["speed"]) * delta
		obstacles[i]["rect"] = rect
		var nearest := Vector2(clampf(player_x, rect.position.x, rect.end.x), clampf(PLAYER_Y, rect.position.y, rect.end.y))
		if nearest.distance_squared_to(Vector2(player_x, PLAYER_Y)) <= PLAYER_RADIUS * PLAYER_RADIUS:
			end_game()
			break
		if rect.position.y > HEIGHT + 60.0:
			obstacles.remove_at(i)
	score_label.text = "TIME  %d" % int(elapsed)
	queue_redraw()


func end_game() -> void:
	playing = false
	if int(elapsed) > best:
		best = int(elapsed)
		var save := ConfigFile.new()
		save.set_value("game", "best", best)
		save.save("user://score.cfg")
	best_label.text = "BEST  %d" % best
	title_label.text = "GAME OVER"
	message_label.text = "You survived %d seconds.\nBest: %d seconds" % [int(elapsed), best]
	start_button.text = "TRY AGAIN"
	set_menu_visible(true)


func _draw() -> void:
	for i in range(55):
		var star := Vector2(fmod(float(i * 137 + 31), WIDTH), fmod(float(i * 97 + 17) + elapsed * 16.0, HEIGHT))
		draw_circle(star, 1.2 if i % 3 else 2.0, Color(0.4, 0.57, 0.8, 0.45))
	draw_line(Vector2(16, 730), Vector2(464, 730), Color("23354e"), 1.0)
	for obstacle in obstacles:
		var rect: Rect2 = obstacle["rect"]
		draw_style_box(block_style(), rect)
		draw_line(rect.position + Vector2(8, 8), rect.position + Vector2(rect.size.x - 8, 8), Color("ffd3ac"), 3.0)
	var center := Vector2(player_x, PLAYER_Y)
	draw_circle(center, 27.0, Color(0.3, 0.9, 1.0, 0.10))
	draw_circle(center, PLAYER_RADIUS, Color("65e5f5"))
	draw_circle(center + Vector2(-5, -5), 5.0, Color("e3fcff"))


func block_style() -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = Color("f47d69")
	style.set_corner_radius_all(6)
	return style
