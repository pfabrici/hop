package org.apache.hop.metadata.serializer.memory;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.metadata.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryMetadataSerializer<T extends IHopMetadata> implements IHopMetadataSerializer<T> {

  private final IVariables variables;
  private IHopMetadataProvider metadataProvider;
  private Class<T> managedClass;
  private Map<String, T> objectMap;

  public MemoryMetadataSerializer( IHopMetadataProvider provider, Class<T> managedClass, IVariables variables ) {
    this.metadataProvider = provider;
    this.managedClass = managedClass;
    this.variables = variables;
    objectMap = new HashMap<>();
  }

  @Override public List<T> loadAll() throws HopException {
    List<T> list = new ArrayList<>();
    for ( String name : listObjectNames() ) {
      list.add( load( name ) );
    }
    return list;
  }

  @Override public T load( String name ) throws HopException {
    if ( name == null ) {
      throw new HopException( "Error: you need to specify the name of the metadata object to load" );
    }
    if ( !exists( name ) ) {
      return null;
    }
    T t = objectMap.get( name );

    if ( t instanceof IVariables ) {
      ( (IVariables) t ).initializeVariablesFrom( variables );
    }

    return t;
  }

  @Override public void save( T object ) throws HopException {
    String name = ReflectionUtil.getObjectName( object );
    objectMap.put( name, object );
  }

  @Override public T delete( String name ) throws HopException {
    if ( name == null ) {
      throw new HopException( "Error: you need to specify the name of the metadata object to delete" );
    }
    if ( !exists( name ) ) {
      throw new HopException( "Error: Object '" + name + "' doesn't exist" );
    }
    T t = objectMap.get( name );
    objectMap.remove( name );
    return t;
  }

  @Override public List<String> listObjectNames() throws HopException {
    List<String> names = new ArrayList<>();
    names.addAll( objectMap.keySet() );
    return names;
  }

  @Override public boolean exists( String name ) throws HopException {
    return objectMap.containsKey( name );
  }

  /**
   * Gets metadataProvider
   *
   * @return value of metadataProvider
   */
  @Override public IHopMetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  /**
   * @param metadataProvider The metadataProvider to set
   */
  public void setMetadataProvider( IHopMetadataProvider metadataProvider ) {
    this.metadataProvider = metadataProvider;
  }

  /**
   * Gets managedClass
   *
   * @return value of managedClass
   */
  @Override public Class<T> getManagedClass() {
    return managedClass;
  }

  /**
   * @param managedClass The managedClass to set
   */
  public void setManagedClass( Class<T> managedClass ) {
    this.managedClass = managedClass;
  }

  /**
   * Gets objectMap
   *
   * @return value of objectMap
   */
  public Map<String, T> getObjectMap() {
    return objectMap;
  }

  /**
   * @param objectMap The objectMap to set
   */
  public void setObjectMap( Map<String, T> objectMap ) {
    this.objectMap = objectMap;
  }

  /**
   * Gets variables
   *
   * @return value of variables
   */
  public IVariables getVariables() {
    return variables;
  }
}
