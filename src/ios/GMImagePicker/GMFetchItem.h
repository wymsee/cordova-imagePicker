//
//  GMFetchItem.h
//  GMPhotoPicker
//
//  Created by micheladrion on 4/26/15.
//  Copyright (c) 2015 Guillermo Muntaner Perelló. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Photos/Photos.h>


@interface GMFetchItem : NSObject

@property (nonatomic, assign) bool be_progressed;
@property (nonatomic, assign) bool be_finished;
@property (nonatomic, assign) double percent;
@property (nonatomic, strong) NSString * image_fullsize;
@property (nonatomic, strong) NSString * image_thumb;


@property (nonatomic, assign) bool be_saving_img_thumb;
@property (nonatomic, assign) bool be_saving_img;

@end
