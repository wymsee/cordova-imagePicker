//
//  GMGridViewController.h
//  GMPhotoPicker
//
//  Created by Guillermo Muntaner Perelló on 19/09/14.
//  Copyright (c) 2014 Guillermo Muntaner Perelló. All rights reserved.
//


#import "GMImagePickerController.h"
#import "UIImage+fixOrientation.m"

#import <Photos/Photos.h>



@interface GMGridViewController : UICollectionViewController

@property (strong) PHFetchResult *assetsFetchResults;
@property (nonatomic, weak) NSMutableDictionary * dic_asset_fetches;

-(id)initWithPicker:(GMImagePickerController *)picker;
    
@end