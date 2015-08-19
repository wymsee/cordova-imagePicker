//
//  GMPHAsset.m
//  GMPhotoPicker
//
//  Created by micheladrion on 4/24/15.
//  Copyright (c) 2015 Guillermo Muntaner Perelló. All rights reserved.
//


#import "GMPHAsset.h"

@implementation PHAsset (GMPHAsset)

ADD_DYNAMIC_PROPERTY(NSNumber *,cell,setCell);
ADD_DYNAMIC_PROPERTY(NSNumber *,be_progressed,setBe_progressed);
ADD_DYNAMIC_PROPERTY(NSNumber *,be_finished,setBe_finished);
ADD_DYNAMIC_PROPERTY(NSNumber *,percent,setPercent);
ADD_DYNAMIC_PROPERTY(UIImage *,image_fullsize,setImage_fullsize);
ADD_DYNAMIC_PROPERTY(UIImage *,image_thumb,setImage_thumb);

@end