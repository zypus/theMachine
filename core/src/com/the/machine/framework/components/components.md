# The Components defined atm are

  * **Component**
  * **DisabledComponent**
  * **IgnoredComponent** -> If set, the Entity is ignored in the WorldTreeSystem
  * **LayerComponent**
  * **ObservableComponent**
    * **AbstractComponent**
      * **CameraComponent**
      * **CanvasComponent** -> Component on top of the rendered objects. Can for example also have the ButtonComponent or a LabelComponent
      * **ScreenComponent**
      * **SpriteRenderComponent** -> For sprites which can be viewed by a camera
      * **WorldComponent**
    * **DimensionComponent**
    * **NameComponent** -> Sets a name for a component e.g. "Main Camera", "Guard #1"
    * **TransformComponent** -> Sets the position, rotation and scale for an Entity
  * **ParentComponent**
  * **SubEntityComponent**
  * **WorldTreeComponent** -> Makes the Entity visible in the WorldTree
