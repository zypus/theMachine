# The Components defined atm are

  * **Component** -> Imported from Ashley. Not directly used
  * **DisabledComponent** -> A disabled Entity is ignored by all the Systems
  * **IgnoredComponent** -> If set, the Entity is ignored in the WorldTreeSystem
  * **LayerComponent** -> Creates a layer for sprites (?)
  * *ObservableComponent* -> Used for Entities that are observed by a system (?). Not used directly
    * *AbstractComponent*
      * **CameraComponent** -> Makes a camera from an Entity
      * **CanvasComponent** -> Component on top of the rendered objects. Can for example also have the ButtonComponent or a LabelComponent
      * **ScreenComponent** -> (?)
      * **SpriteRenderComponent** -> For sprites which can be viewed by a camera
      * **WorldComponent** -> Used for world Entities
    * **DimensionComponent** -> Used for Table and Canvas Entities
    * **NameComponent** -> Sets a name for a component e.g. "Main Camera", "Guard #1"
    * **TransformComponent** -> Sets the position, rotation and scale for an Entity
  * **ParentComponent** -> Used to make an Entity a Parent of a SubEntity
  * **SubEntityComponent** -> To make an Entity the SubEntity of a Parent
  * **WorldTreeComponent** -> Makes the Entity visible in the WorldTree
