//
//  GMPHAsset.h
//  GMPhotoPicker
//
//  Created by micheladrion on 4/24/15.
//  Copyright (c) 2015 Guillermo Muntaner Perelló. All rights reserved.
//

#define ADD_DYNAMIC_PROPERTY(PROPERTY_TYPE,PROPERTY_NAME,SETTER_NAME) \
@dynamic PROPERTY_NAME ; \
static char kProperty##PROPERTY_NAME; \
- ( PROPERTY_TYPE ) PROPERTY_NAME \
{ \
return ( PROPERTY_TYPE ) objc_getAssociatedObject(self, &(kProperty##PROPERTY_NAME ) ); \
} \
\
- (void) SETTER_NAME :( PROPERTY_TYPE ) PROPERTY_NAME \
{ \
objc_setAssociatedObject(self, &kProperty##PROPERTY_NAME , PROPERTY_NAME , OBJC_ASSOCIATION_RETAIN); \
} \

#import <objc/runtime.h>

#import <Photos/PHAsset.h>

@interface PHAsset (GMPHAsset)


@property (nonatomic, assign) id cell;
@property (nonatomic, assign) NSNumber *be_progressed;
@property (nonatomic, assign) NSNumber *be_finished;
@property (nonatomic, assign) NSNumber *percent;
@property (nonatomic, strong) UIImage * image_fullsize;
@property (nonatomic, strong) UIImage * image_thumb;

@end
