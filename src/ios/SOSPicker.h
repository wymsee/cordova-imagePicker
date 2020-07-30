//
//  SOSPicker.h
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import <Cordova/CDVPlugin.h>
#import "TZImagePickerController.h"

@interface SOSPicker : CDVPlugin <TZImagePickerControllerDelegate>

@property (copy) NSString* callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command;

@property (nonatomic, assign) NSInteger width;
@property (nonatomic, assign) NSInteger height;
@property (nonatomic, assign) NSInteger quality;

@end
